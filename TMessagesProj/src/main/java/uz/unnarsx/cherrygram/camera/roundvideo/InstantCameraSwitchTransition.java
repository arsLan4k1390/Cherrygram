/**
 * This is the source code of Cherrygram for Android.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Please, be respectful and credit the original author if you use this code.
 *
 * Copyright github.com/arsLan4k1390, 2022-2026.
 */

package uz.unnarsx.cherrygram.camera.roundvideo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.os.SystemClock;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.ui.Components.InstantCameraVideoEncoderOverlayHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class InstantCameraSwitchTransition {

    private static final int BLUR_SIZE = 48;
    private static final float BACKGROUND_BLUR_RADIUS = 4f;
    private static final float TARGET_BACKGROUND_BLUR_FRACTION = 1.15f;
    private static final int GAUSSIAN_KERNEL_RADIUS = 9;
    private static final float GAUSSIAN_SIGMA = 4f;
    private static final int OLD_MIP_SIZE = 512;
    private static final float OLD_BLUR_STEP_SCALE = 0.625f;
    private static final float OLD_MIP_LOD_OFFSET = 1.25f;
    private static final float OLD_SHARP_BLEND_RADIUS = 2f;

    private static final long FRAME_DELAY_MS = 33L;
    private static final float OVERDUE_BLUR_GROWTH = 0.35f;
    private static final int MIN_REVEAL_MS = 210;
    private static final int MAX_REVEAL_MS = 300;

    private static final int STATE_NONE = 0;
    private static final int STATE_BLUR_OLD = 1;
    private static final int STATE_WAIT_NEW = 2;
    private static final int STATE_CROSSFADE = 3;
    private static final int STATE_UNBLUR_NEW = 4;

    private static final int TEXTURE_OLD = 0;
    private static final int TEXTURE_OLD_MIP = 1;
    private static final int TEXTURE_NEW = 2;
    private static final int TEXTURE_NEW_BLUR = 3;
    private static final int TEXTURE_NEW_BLUR_TEMP = 4;

    private static final int FRAME_BUFFER_TARGET = 5;

    private static final String VERTEX_SHADER =
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            "varying vec2 vTextureCoord;\n" +
            "void main() {\n" +
            "    gl_Position = aPosition;\n" +
            "    vTextureCoord = aTextureCoord.xy;\n" +
            "}\n";

    private static final String COPY_FRAGMENT_SHADER =
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";

    private static final String TRANSITION_FRAGMENT_SHADER =
            "#ifdef GL_FRAGMENT_PRECISION_HIGH\n" +
            "precision highp float;\n" +
            "#else\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform sampler2D sTexture;\n" +
            "uniform sampler2D omTexture;\n" +
            "uniform sampler2D nTexture;\n" +
            "uniform sampler2D nbTexture;\n" +
            "uniform vec2 oldBlurStep;\n" +
            "uniform float oldMipBias;\n" +
            "uniform float oldBlurMix;\n" +
            "uniform float newBlur;\n" +
            "uniform float mixValue;\n" +
            "vec4 sampleOldFrame() {\n" +
            "    vec2 x = vec2(oldBlurStep.x, 0.0);\n" +
            "    vec2 diagonalUp = vec2(oldBlurStep.x * 0.5, oldBlurStep.y * 0.8660254);\n" +
            "    vec2 diagonalDown = vec2(oldBlurStep.x * 0.5, -oldBlurStep.y * 0.8660254);\n" +
            "    vec4 color = sampleOldMip(omTexture, vTextureCoord, oldMipBias) * 0.25;\n" +
            "    color += (sampleOldMip(omTexture, vTextureCoord + x, oldMipBias)\n" +
            "            + sampleOldMip(omTexture, vTextureCoord - x, oldMipBias)\n" +
            "            + sampleOldMip(omTexture, vTextureCoord + diagonalUp, oldMipBias)\n" +
            "            + sampleOldMip(omTexture, vTextureCoord - diagonalUp, oldMipBias)\n" +
            "            + sampleOldMip(omTexture, vTextureCoord + diagonalDown, oldMipBias)\n" +
            "            + sampleOldMip(omTexture, vTextureCoord - diagonalDown, oldMipBias)) * 0.125;\n" +
            "    return mix(texture2D(sTexture, vTextureCoord), color, oldBlurMix);\n" +
            "}\n" +
            "void main() {\n" +
            "    vec4 oldFrame = sampleOldFrame();\n" +
            "    vec4 newFrame = mix(texture2D(nTexture, vTextureCoord),\n" +
            "            texture2D(nbTexture, vTextureCoord), newBlur);\n" +
            "    gl_FragColor = vec4(mix(oldFrame, newFrame, mixValue).rgb, 1.0);\n" +
            "}\n";

    private final int size;
    private final int newBlurSize;
    private final boolean explicitTextureLod;
    private final Program copyProgram;
    private final BlurProgram blurProgram;
    private final TransitionProgram transitionProgram;
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer textureBuffer;
    private final int[] textures = new int[5];
    private final int[] frameBuffers = new int[6];

    private int state = STATE_NONE;
    private boolean toFront;
    private long switchStartNs;
    private long stageStartNs;
    private long crossfadeDurationNs;
    private long unblurDurationNs;
    private int expectedWaitMs;
    private float oldBlurRadiusPx;
    private boolean newFrameCaptured;
    private float newBlurRadiusPx = -1f;

    public InstantCameraSwitchTransition(int size) {
        this.size = size;
        newBlurSize = Math.max(1, size / 2);
        explicitTextureLod = supportsExplicitTextureLod();
        copyProgram = new Program(COPY_FRAGMENT_SHADER);
        blurProgram = new BlurProgram();
        transitionProgram = new TransitionProgram(explicitTextureLod);

        vertexBuffer = createBuffer(new float[]{
                -1f, -1f, 0f,
                1f, -1f, 0f,
                -1f, 1f, 0f,
                1f, 1f, 0f
        });
        textureBuffer = createBuffer(new float[]{
                0f, 0f,
                1f, 0f,
                0f, 1f,
                1f, 1f
        });

        GLES20.glGenTextures(textures.length, textures, 0);
        createColorTexture(TEXTURE_OLD, size);
        createColorTexture(TEXTURE_OLD_MIP, OLD_MIP_SIZE);
        createColorTexture(TEXTURE_NEW, size);
        createColorTexture(TEXTURE_NEW_BLUR, newBlurSize);
        createColorTexture(TEXTURE_NEW_BLUR_TEMP, newBlurSize);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glGenFramebuffers(frameBuffers.length, frameBuffers, 0);
        for (int i = 0; i < textures.length; i++) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[i]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textures[i], 0);
            GLES20.glClearColor(0f, 0f, 0f, 1f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public boolean begin(int frameTexture, boolean toFront) {
        if (state != STATE_NONE) {
            return false;
        }
        GLES20.glDisable(GLES20.GL_BLEND);
        setTextureMinFilter(TEXTURE_OLD_MIP, GLES20.GL_LINEAR);
        renderCopy(frameTexture, TEXTURE_OLD, size);
        renderCopy(textures[TEXTURE_OLD], TEXTURE_OLD_MIP, OLD_MIP_SIZE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[TEXTURE_OLD_MIP]);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        this.toFront = toFront;
        newFrameCaptured = false;
        newBlurRadiusPx = -1f;
        oldBlurRadiusPx = 0f;
        state = STATE_BLUR_OLD;
        switchStartNs = stageStartNs = SystemClock.elapsedRealtimeNanos();
        configureTiming(SwitchTimings.getAverageMs(toFront));
        return true;
    }

    public boolean isActive() {
        return state != STATE_NONE;
    }

    public boolean isWaitingForCamera() {
        return state == STATE_BLUR_OLD || state == STATE_WAIT_NEW;
    }

    public boolean onCameraFrame(int frameTexture) {
        if (state == STATE_NONE) {
            return false;
        }
        GLES20.glDisable(GLES20.GL_BLEND);
        renderCopy(frameTexture, TEXTURE_NEW, size);
        newBlurRadiusPx = -1f;
        if (newFrameCaptured) {
            return false;
        }
        newFrameCaptured = true;
        long now = SystemClock.elapsedRealtimeNanos();
        SwitchTimings.record(toFront, Math.round((now - switchStartNs) / 1_000_000f));
        state = STATE_CROSSFADE;
        stageStartNs = now;
        return true;
    }

    public boolean render(int targetTexture) {
        if (state == STATE_NONE) {
            return true;
        }
        long now = SystemClock.elapsedRealtimeNanos();
        long elapsed = now - stageStartNs;
        float newBlur = 1f;
        float mix = 0f;
        if (state == STATE_BLUR_OLD) {
            oldBlurRadiusPx = getOldBlurRadiusPx(now);
            if (elapsed >= getExpectedWaitNs()) {
                state = STATE_WAIT_NEW;
                stageStartNs = now;
            }
        } else if (state == STATE_WAIT_NEW) {
            oldBlurRadiusPx = getOldBlurRadiusPx(now);
        } else if (elapsed < crossfadeDurationNs) {
            mix = smoothStep(elapsed / (float) crossfadeDurationNs);
        } else {
            state = STATE_UNBLUR_NEW;
            mix = 1f;
            newBlur = 1f - smoothStep((elapsed - crossfadeDurationNs) / (float) unblurDurationNs);
        }

        GLES20.glDisable(GLES20.GL_BLEND);
        if (newFrameCaptured) {
            renderNewBlur(Math.min(oldBlurRadiusPx, GAUSSIAN_KERNEL_RADIUS));
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[FRAME_BUFFER_TARGET]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, targetTexture, 0);
        GLES20.glViewport(0, 0, size, size);
        TransitionProgram program = transitionProgram;
        GLES20.glUseProgram(program.program);
        bindTexture(0, textures[TEXTURE_OLD]);
        bindTexture(1, textures[TEXTURE_OLD_MIP]);
        bindTexture(2, textures[TEXTURE_NEW]);
        bindTexture(3, textures[TEXTURE_NEW_BLUR]);
        float oldBlurStep = Math.max(0f, oldBlurRadiusPx) * OLD_BLUR_STEP_SCALE / size;
        GLES20.glUniform2f(program.oldBlurStepHandle, oldBlurStep, oldBlurStep);
        GLES20.glUniform1f(program.oldMipBiasHandle, getOldMipParameter(oldBlurRadiusPx));
        GLES20.glUniform1f(program.oldBlurMixHandle, clamp01(oldBlurRadiusPx / OLD_SHARP_BLEND_RADIUS));
        GLES20.glUniform1f(program.newBlurHandle, newBlur);
        GLES20.glUniform1f(program.mixHandle, mix);
        draw(program);
        for (int unit = 3; unit >= 0; unit--) {
            bindTexture(unit, 0);
        }

        if (state == STATE_UNBLUR_NEW && elapsed >= crossfadeDurationNs + unblurDurationNs) {
            state = STATE_NONE;
            newFrameCaptured = false;
            return true;
        }
        return false;
    }

    public long getNextFrameDelayMs() {
        if (state != STATE_BLUR_OLD) {
            return FRAME_DELAY_MS;
        }
        long remainingNs = getExpectedWaitNs() - (SystemClock.elapsedRealtimeNanos() - stageStartNs);
        return Math.min(FRAME_DELAY_MS, Math.max(1L, (remainingNs + 999_999L) / 1_000_000L));
    }

    public void cancel() {
        state = STATE_NONE;
        newFrameCaptured = false;
    }

    public void destroy() {
        copyProgram.destroy();
        blurProgram.destroy();
        transitionProgram.destroy();
        GLES20.glDeleteTextures(textures.length, textures, 0);
        GLES20.glDeleteFramebuffers(frameBuffers.length, frameBuffers, 0);
    }

    private void renderCopy(int sourceTexture, int targetTextureIndex, int targetSize) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[targetTextureIndex]);
        GLES20.glViewport(0, 0, targetSize, targetSize);
        GLES20.glUseProgram(copyProgram.program);
        bindTexture(0, sourceTexture);
        draw(copyProgram);
        bindTexture(0, 0);
    }

    private void renderNewBlur(float radiusPx) {
        if (Math.abs(newBlurRadiusPx - radiusPx) < 0.2f) {
            return;
        }
        float offset = radiusPx / (GAUSSIAN_KERNEL_RADIUS * size);
        renderBlurPass(TEXTURE_NEW, TEXTURE_NEW_BLUR_TEMP, offset, 0f);
        renderBlurPass(TEXTURE_NEW_BLUR_TEMP, TEXTURE_NEW_BLUR, 0f, offset);
        newBlurRadiusPx = radiusPx;
    }

    private void renderBlurPass(int sourceTextureIndex, int targetTextureIndex, float offsetX, float offsetY) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[targetTextureIndex]);
        GLES20.glViewport(0, 0, newBlurSize, newBlurSize);
        GLES20.glUseProgram(blurProgram.program);
        bindTexture(0, textures[sourceTextureIndex]);
        GLES20.glUniform2f(blurProgram.offsetHandle, offsetX, offsetY);
        draw(blurProgram);
        bindTexture(0, 0);
    }

    private void draw(Program program) {
        GLES20.glVertexAttribPointer(program.positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer.position(0));
        GLES20.glEnableVertexAttribArray(program.positionHandle);
        GLES20.glVertexAttribPointer(program.textureCoordHandle, 2, GLES20.GL_FLOAT, false, 8, textureBuffer.position(0));
        GLES20.glEnableVertexAttribArray(program.textureCoordHandle);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(program.textureCoordHandle);
        GLES20.glDisableVertexAttribArray(program.positionHandle);
        GLES20.glUseProgram(0);
    }

    private float getOldBlurRadiusPx(long nowNs) {
        long elapsedNs = Math.max(0L, nowNs - switchStartNs);
        long expectedNs = getExpectedWaitNs();
        float targetRadius = BACKGROUND_BLUR_RADIUS * size / BLUR_SIZE * TARGET_BACKGROUND_BLUR_FRACTION;
        if (elapsedNs <= expectedNs) {
            return targetRadius * easeOutQuart(elapsedNs / (float) expectedNs);
        }
        float overdue = (elapsedNs - expectedNs) / (float) expectedNs;
        return targetRadius * (float) Math.sqrt(1f + OVERDUE_BLUR_GROWTH * Math.log1p(overdue));
    }

    private float getOldMipParameter(float radiusPx) {
        float sampleStepTexels = Math.max(1f, radiusPx) * OLD_BLUR_STEP_SCALE * OLD_MIP_SIZE / size;
        float desiredLod = Math.min(8f, (float) (Math.log(sampleStepTexels) / Math.log(2.0))) + OLD_MIP_LOD_OFFSET;
        if (explicitTextureLod) {
            return Math.max(0f, desiredLod);
        }
        float implicitLod = (float) (Math.log(OLD_MIP_SIZE / (double) size) / Math.log(2.0));
        return Math.max(0f, desiredLod - implicitLod);
    }

    private long getExpectedWaitNs() {
        return Math.max(FRAME_DELAY_MS, expectedWaitMs) * 1_000_000L;
    }

    private void configureTiming(int expectedWaitMs) {
        this.expectedWaitMs = expectedWaitMs;
        int revealMs = Math.max(MIN_REVEAL_MS, Math.min(MAX_REVEAL_MS, MAX_REVEAL_MS - Math.max(0, expectedWaitMs - 400) * 3 / 20));
        int crossfadeMs = revealMs * 45 / 100;
        crossfadeDurationNs = crossfadeMs * 1_000_000L;
        unblurDurationNs = (revealMs - crossfadeMs) * 1_000_000L;
    }

    private void createColorTexture(int textureIndex, int textureSize) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[textureIndex]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, textureSize, textureSize, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
    }

    private void setTextureMinFilter(int textureIndex, int filter) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[textureIndex]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, filter);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    private static void bindTexture(int unit, int texture) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
    }

    private static FloatBuffer createBuffer(float[] values) {
        FloatBuffer buffer = ByteBuffer.allocateDirect(values.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buffer.put(values).position(0);
        return buffer;
    }

    private static boolean supportsExplicitTextureLod() {
        String extensions = GLES20.glGetString(GLES20.GL_EXTENSIONS);
        return extensions != null && extensions.contains("GL_EXT_shader_texture_lod");
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static float smoothStep(float value) {
        float clamped = clamp01(value);
        return clamped * clamped * (3f - 2f * clamped);
    }

    private static float easeOutQuart(float value) {
        float inverse = 1f - clamp01(value);
        return 1f - inverse * inverse * inverse * inverse;
    }

    private static String createTransitionFragmentShader(boolean explicitTextureLod) {
        if (explicitTextureLod) {
            return "#extension GL_EXT_shader_texture_lod : require\n"
                    + TRANSITION_FRAGMENT_SHADER.replace("sampleOldMip", "texture2DLodEXT");
        }
        return TRANSITION_FRAGMENT_SHADER.replace("sampleOldMip", "texture2D");
    }

    private static String createGaussianBlurFragmentShader(int radius, float sigma) {
        double weightSum = 0.0;
        for (int i = -radius; i <= radius; i++) {
            weightSum += Math.exp(-(i * i) / (2.0 * sigma * sigma));
        }
        StringBuilder shader = new StringBuilder(4096);
        shader.append("precision mediump float;\n")
                .append("varying vec2 vTextureCoord;\n")
                .append("uniform sampler2D sTexture;\n")
                .append("uniform vec2 texOffset;\n")
                .append("void main() {\n");
        shader.append("    vec3 color = texture2D(sTexture, vTextureCoord).rgb * ")
                .append((float) (1.0 / weightSum))
                .append(";\n");
        for (int i = 1; i <= radius; i += 2) {
            float firstWeight = (float) (Math.exp(-(i * i) / (2.0 * sigma * sigma)) / weightSum);
            int second = i + 1;
            float secondWeight = second <= radius
                    ? (float) (Math.exp(-(second * second) / (2.0 * sigma * sigma)) / weightSum)
                    : 0f;
            float combinedWeight = firstWeight + secondWeight;
            float sampleOffset = secondWeight == 0f
                    ? i
                    : (i * firstWeight + second * secondWeight) / combinedWeight;
            shader.append("    color += (texture2D(sTexture, vTextureCoord + texOffset * ")
                    .append(sampleOffset)
                    .append(").rgb + texture2D(sTexture, vTextureCoord - texOffset * ")
                    .append(sampleOffset)
                    .append(").rgb) * ")
                    .append(combinedWeight)
                    .append(";\n");
        }
        shader.append("    gl_FragColor = vec4(color, 1.0);\n")
                .append("}\n");
        return shader.toString();
    }

    private static class TransitionProgram extends Program {
        final int oldBlurStepHandle;
        final int oldMipBiasHandle;
        final int oldBlurMixHandle;
        final int newBlurHandle;
        final int mixHandle;

        TransitionProgram(boolean explicitTextureLod) {
            super(createTransitionFragmentShader(explicitTextureLod));
            oldBlurStepHandle = GLES20.glGetUniformLocation(program, "oldBlurStep");
            oldMipBiasHandle = GLES20.glGetUniformLocation(program, "oldMipBias");
            oldBlurMixHandle = GLES20.glGetUniformLocation(program, "oldBlurMix");
            newBlurHandle = GLES20.glGetUniformLocation(program, "newBlur");
            mixHandle = GLES20.glGetUniformLocation(program, "mixValue");
            GLES20.glUseProgram(program);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "omTexture"), 1);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "nTexture"), 2);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "nbTexture"), 3);
            GLES20.glUseProgram(0);
        }
    }

    private static class BlurProgram extends Program {
        final int offsetHandle;

        BlurProgram() {
            super(createGaussianBlurFragmentShader(GAUSSIAN_KERNEL_RADIUS, GAUSSIAN_SIGMA));
            offsetHandle = GLES20.glGetUniformLocation(program, "texOffset");
        }
    }

    private static class Program {
        final int program;
        final int vertexShader;
        final int fragmentShader;
        final int positionHandle;
        final int textureCoordHandle;

        Program(String fragmentShaderSource) {
            vertexShader = createShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
            fragmentShader = createShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);
            program = GLES20.glCreateProgram();
            GLES20.glAttachShader(program, vertexShader);
            GLES20.glAttachShader(program, fragmentShader);
            GLES20.glLinkProgram(program);
            int[] status = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
            if (status[0] == 0) {
                String log = GLES20.glGetProgramInfoLog(program);
                destroy();
                throw new IllegalStateException("InstantCameraSwitchTransition: unable to link program: " + log);
            }
            positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
            textureCoordHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
            GLES20.glUseProgram(program);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "sTexture"), 0);
            GLES20.glUseProgram(0);
        }

        void destroy() {
            GLES20.glDeleteProgram(program);
            GLES20.glDeleteShader(vertexShader);
            GLES20.glDeleteShader(fragmentShader);
        }
    }

    private static int createShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] == 0) {
            String log = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new IllegalStateException("InstantCameraSwitchTransition: unable to compile shader: " + log);
        }
        return shader;
    }

    private static final class SwitchTimings {

        private static final int SAMPLE_COUNT = 8;
        private static final int MIN_SAMPLE_MS = 200;
        private static final int MAX_SAMPLE_MS = 2_000;
        private static final int DEFAULT_TO_FRONT_MS = 680;
        private static final int DEFAULT_TO_BACK_MS = 630;
        private static final String[] KEYS = {
                "instant_camera_switch_to_back_ms",
                "instant_camera_switch_to_front_ms"
        };

        private static final int[][] samples = new int[2][SAMPLE_COUNT];
        private static final int[] counts = new int[2];
        private static final int[] nextIndices = new int[2];
        private static boolean loaded;

        static synchronized int getAverageMs(boolean toFront) {
            ensureLoaded();
            int direction = toFront ? 1 : 0;
            int count = counts[direction];
            if (count == 0) {
                return toFront ? DEFAULT_TO_FRONT_MS : DEFAULT_TO_BACK_MS;
            }
            int sum = 0;
            for (int i = 0; i < count; i++) {
                sum += samples[direction][i];
            }
            return Math.round(sum / (float) count);
        }

        static synchronized void record(boolean toFront, int durationMs) {
            ensureLoaded();
            int direction = toFront ? 1 : 0;
            samples[direction][nextIndices[direction]] = Math.max(MIN_SAMPLE_MS, Math.min(MAX_SAMPLE_MS, durationMs));
            nextIndices[direction] = (nextIndices[direction] + 1) % SAMPLE_COUNT;
            if (counts[direction] < SAMPLE_COUNT) {
                counts[direction]++;
            }
            int count = counts[direction];
            int first = count == SAMPLE_COUNT ? nextIndices[direction] : 0;
            StringBuilder value = new StringBuilder(count * 5);
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    value.append(',');
                }
                value.append(samples[direction][(first + i) % SAMPLE_COUNT]);
            }
            preferences().edit().putString(KEYS[direction], value.toString()).apply();
        }

        private static void ensureLoaded() {
            if (loaded) {
                return;
            }
            loaded = true;
            SharedPreferences preferences = preferences();
            for (int direction = 0; direction < KEYS.length; direction++) {
                String stored = preferences.getString(KEYS[direction], null);
                if (stored == null || stored.isEmpty()) {
                    continue;
                }
                String[] values = stored.split(",");
                for (int i = Math.max(0, values.length - SAMPLE_COUNT); i < values.length; i++) {
                    try {
                        int parsed = Integer.parseInt(values[i]);
                        samples[direction][counts[direction]++] = Math.max(MIN_SAMPLE_MS, Math.min(MAX_SAMPLE_MS, parsed));
                    } catch (NumberFormatException e) {
                        FileLog.e(e);
                    }
                }
                nextIndices[direction] = counts[direction] % SAMPLE_COUNT;
            }
        }

        private static SharedPreferences preferences() {
            return ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        }
    }
}
