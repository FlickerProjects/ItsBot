package io.github.itsflicker.itsbot.util

import java.awt.image.BufferedImage
import java.awt.image.Raster
import kotlin.math.exp
import kotlin.math.sqrt

object EffectUtils {

    // =================================================================================================================
    // Blur
    /**
     * Apply Gaussian Blur to Image
     *
     * @param src    The image tp
     * @param destination   The destination image to draw blured src image into, null if you want a new one created
     * @param radius The blur kernel radius
     * @return The blured image
     */
    fun gaussianBlur(src: BufferedImage, destination: BufferedImage?, radius: Int): BufferedImage {
        var dst = destination
        val width = src.width
        val height = src.height
        if (dst == null || dst.width != width || dst.height != height || src.type != dst.type) {
            dst = createColorModelCompatibleImage(src)
        }
        val kernel = createGaussianKernel(radius)
        when (src.type) {
            BufferedImage.TYPE_INT_ARGB -> {
                val srcPixels = IntArray(width * height)
                val dstPixels = IntArray(width * height)
                getPixels(src, 0, 0, width, height, srcPixels)
                // horizontal pass
                blur(srcPixels, dstPixels, width, height, kernel, radius)
                // vertical pass
                blur(dstPixels, srcPixels, height, width, kernel, radius)
                // the result is now stored in srcPixels due to the 2nd pass
                setPixels(dst, 0, 0, width, height, srcPixels)
            }
            BufferedImage.TYPE_BYTE_GRAY -> {
                val srcPixels = ByteArray(width * height)
                val dstPixels = ByteArray(width * height)
                getPixels(src, 0, 0, width, height, srcPixels)
                // horizontal pass
                blur(srcPixels, dstPixels, width, height, kernel, radius)
                // vertical pass
                blur(dstPixels, srcPixels, height, width, kernel, radius)
                // the result is now stored in srcPixels due to the 2nd pass
                setPixels(dst, 0, 0, width, height, srcPixels)
            }
            else -> {
                throw IllegalArgumentException(
                    "EffectUtils.gaussianBlur() src image is not a supported type, type=[" +
                            src.type + "]"
                )
            }
        }
        return dst
    }

    /**
     *
     * Blurs the source pixels into the destination pixels. The force of the blur is specified by the radius which
     * must be greater than 0.
     *
     *The source and destination pixels arrays are expected to be in the INT_ARGB
     * format.
     *
     *After this method is executed, dstPixels contains a transposed and filtered copy of
     * srcPixels.
     *
     * @param srcPixels the source pixels
     * @param dstPixels the destination pixels
     * @param width     the width of the source picture
     * @param height    the height of the source picture
     * @param kernel    the kernel of the blur effect
     * @param radius    the radius of the blur effect
     */
    private fun blur(
        srcPixels: IntArray, dstPixels: IntArray,
        width: Int, height: Int,
        kernel: FloatArray, radius: Int
    ) {
        var a: Float
        var r: Float
        var g: Float
        var b: Float
        var ca: Int
        var cr: Int
        var cg: Int
        var cb: Int
        for (y in 0 until height) {
            var index = y
            val offset = y * width
            for (x in 0 until width) {
                b = 0.0f
                g = b
                r = g
                a = r
                for (i in -radius..radius) {
                    var subOffset = x + i
                    if (subOffset < 0 || subOffset >= width) {
                        subOffset = (x + width) % width
                    }
                    val pixel = srcPixels[offset + subOffset]
                    val blurFactor = kernel[radius + i]
                    a += blurFactor * (pixel shr 24 and 0xFF)
                    r += blurFactor * (pixel shr 16 and 0xFF)
                    g += blurFactor * (pixel shr 8 and 0xFF)
                    b += blurFactor * (pixel and 0xFF)
                }
                ca = (a + 0.5f).toInt()
                cr = (r + 0.5f).toInt()
                cg = (g + 0.5f).toInt()
                cb = (b + 0.5f).toInt()
                dstPixels[index] = (if (ca > 255) 255 else ca) shl 24 or
                        ((if (cr > 255) 255 else cr) shl 16) or
                        ((if (cg > 255) 255 else cg) shl 8) or if (cb > 255) 255 else cb
                index += height
            }
        }
    }

    /**
     *
     * Blurs the source pixels into the destination pixels. The force of the blur is specified by the radius which
     * must be greater than 0.
     *
     *The source and destination pixels arrays are expected to be in the BYTE_GREY
     * format.
     *
     *After this method is executed, dstPixels contains a transposed and filtered copy of
     * srcPixels.
     *
     * @param srcPixels the source pixels
     * @param dstPixels the destination pixels
     * @param width     the width of the source picture
     * @param height    the height of the source picture
     * @param kernel    the kernel of the blur effect
     * @param radius    the radius of the blur effect
     */
    private fun blur(
        srcPixels: ByteArray, dstPixels: ByteArray,
        width: Int, height: Int,
        kernel: FloatArray, radius: Int
    ) {
        var p: Float
        var cp: Int
        for (y in 0 until height) {
            var index = y
            val offset = y * width
            for (x in 0 until width) {
                p = 0.0f
                for (i in -radius..radius) {
                    var subOffset = x + i
                    //                    if (subOffset < 0) subOffset = 0;
//                    if (subOffset >= width) subOffset = width-1;
                    if (subOffset < 0 || subOffset >= width) {
                        subOffset = (x + width) % width
                    }
                    val pixel = srcPixels[offset + subOffset].toInt() and 0xFF
                    val blurFactor = kernel[radius + i]
                    p += blurFactor * pixel
                }
                cp = (p + 0.5f).toInt()
                dstPixels[index] = (if (cp > 255) 255 else cp).toByte()
                index += height
            }
        }
    }

    private fun createGaussianKernel(radius: Int): FloatArray {
        require(radius >= 1) { "Radius must be >= 1" }
        val data = FloatArray(radius * 2 + 1)
        val sigma = radius / 3.0f
        val twoSigmaSquare = 2.0f * sigma * sigma
        val sigmaRoot = sqrt(twoSigmaSquare * Math.PI).toFloat()
        var total = 0.0f
        for (i in -radius..radius) {
            val distance = (i * i).toFloat()
            val index = i + radius
            data[index] = exp((-distance / twoSigmaSquare).toDouble()).toFloat() / sigmaRoot
            total += data[index]
        }
        for (i in data.indices) {
            data[i] /= total
        }
        return data
    }

    // =================================================================================================================
    // Get/Set Pixels helper methods
    /**
     *
     * Returns an array of pixels, stored as integers, from a `BufferedImage`. The pixels are grabbed from
     * a rectangular area defined by a location and two dimensions. Calling this method on an image of type different
     * from `BufferedImage.TYPE_INT_ARGB` and `BufferedImage.TYPE_INT_RGB` will unmanage the
     * image.
     *
     * @param img    the source image
     * @param x      the x location at which to start grabbing pixels
     * @param y      the y location at which to start grabbing pixels
     * @param w      the width of the rectangle of pixels to grab
     * @param h      the height of the rectangle of pixels to grab
     * @param p a pre-allocated array of pixels of size w*h; can be null
     * @return `pixels` if non-null, a new array of integers otherwise
     * @throws IllegalArgumentException is `pixels` is non-null and of length &lt; w*h
     */
    private fun getPixels(
        img: BufferedImage,
        x: Int, y: Int, w: Int, h: Int, p: ByteArray?
    ): ByteArray {
        var pixels = p
        if (w == 0 || h == 0) {
            return ByteArray(0)
        }
        if (pixels == null) {
            pixels = ByteArray(w * h)
        } else require(pixels.size >= w * h) { "pixels array must have a length >= w*h" }
        val imageType = img.type
        return if (imageType == BufferedImage.TYPE_BYTE_GRAY) {
            val raster: Raster = img.raster
            raster.getDataElements(x, y, w, h, pixels) as ByteArray
        } else {
            throw IllegalArgumentException("Only type BYTE_GRAY is supported")
        }
    }

    /**
     *
     * Writes a rectangular area of pixels in the destination `BufferedImage`. Calling this method on an
     * image of type different from `BufferedImage.TYPE_INT_ARGB` and `BufferedImage.TYPE_INT_RGB`
     * will unmanage the image.
     *
     * @param img    the destination image
     * @param x      the x location at which to start storing pixels
     * @param y      the y location at which to start storing pixels
     * @param w      the width of the rectangle of pixels to store
     * @param h      the height of the rectangle of pixels to store
     * @param pixels an array of pixels, stored as integers
     * @throws IllegalArgumentException is `pixels` is non-null and of length &lt; w*h
     */
    private fun setPixels(
        img: BufferedImage,
        x: Int, y: Int, w: Int, h: Int, pixels: ByteArray?
    ) {
        if (pixels == null || w == 0 || h == 0) {
            return
        } else require(pixels.size >= w * h) { "pixels array must have a length >= w*h" }
        val imageType = img.type
        if (imageType == BufferedImage.TYPE_BYTE_GRAY) {
            val raster = img.raster
            raster.setDataElements(x, y, w, h, pixels)
        } else {
            throw IllegalArgumentException("Only type BYTE_GRAY is supported")
        }
    }

    /**
     *
     * Returns an array of pixels, stored as integers, from a
     * `BufferedImage`. The pixels are grabbed from a rectangular
     * area defined by a location and two dimensions. Calling this method on
     * an image of type different from `BufferedImage.TYPE_INT_ARGB`
     * and `BufferedImage.TYPE_INT_RGB` will unmanage the image.
     *
     * @param img the source image
     * @param x the x location at which to start grabbing pixels
     * @param y the y location at which to start grabbing pixels
     * @param w the width of the rectangle of pixels to grab
     * @param h the height of the rectangle of pixels to grab
     * @param p a pre-allocated array of pixels of size w*h; can be null
     * @return `pixels` if non-null, a new array of integers
     * otherwise
     * @throws IllegalArgumentException is `pixels` is non-null and
     * of length &lt; w*h
     */
    private fun getPixels(
        img: BufferedImage,
        x: Int, y: Int, w: Int, h: Int, p: IntArray?
    ): IntArray? {
        var pixels = p
        if (w == 0 || h == 0) {
            return IntArray(0)
        }
        if (pixels == null) {
            pixels = IntArray(w * h)
        } else require(pixels.size >= w * h) {
            "pixels array must have a length" +
                    " >= w*h"
        }
        val imageType = img.type
        if (imageType == BufferedImage.TYPE_INT_ARGB ||
            imageType == BufferedImage.TYPE_INT_RGB
        ) {
            val raster: Raster = img.raster
            return raster.getDataElements(x, y, w, h, pixels) as IntArray
        }

        // Unmanages the image
        return img.getRGB(x, y, w, h, pixels, 0, w)
    }

    /**
     *
     * Writes a rectangular area of pixels in the destination
     * `BufferedImage`. Calling this method on
     * an image of type different from `BufferedImage.TYPE_INT_ARGB`
     * and `BufferedImage.TYPE_INT_RGB` will unmanage the image.
     *
     * @param img the destination image
     * @param x the x location at which to start storing pixels
     * @param y the y location at which to start storing pixels
     * @param w the width of the rectangle of pixels to store
     * @param h the height of the rectangle of pixels to store
     * @param pixels an array of pixels, stored as integers
     * @throws IllegalArgumentException is `pixels` is non-null and
     * of length &lt; w*h
     */
    private fun setPixels(
        img: BufferedImage,
        x: Int, y: Int, w: Int, h: Int, pixels: IntArray?
    ) {
        if (pixels == null || w == 0 || h == 0) {
            return
        } else require(pixels.size >= w * h) {
            "pixels array must have a length" +
                    " >= w*h"
        }
        val imageType = img.type
        if (imageType == BufferedImage.TYPE_INT_ARGB ||
            imageType == BufferedImage.TYPE_INT_RGB
        ) {
            val raster = img.raster
            raster.setDataElements(x, y, w, h, pixels)
        } else {
            // Unmanages the image
            img.setRGB(x, y, w, h, pixels, 0, w)
        }
    }

    /**
     *
     * Returns a new `BufferedImage` using the same color model
     * as the image passed as a parameter. The returned image is only compatible
     * with the image passed as a parameter. This does not mean the returned
     * image is compatible with the hardware.
     *
     * @param image the reference image from which the color model of the new
     * image is obtained
     * @return a new `BufferedImage`, compatible with the color model
     * of `image`
     */
    private fun createColorModelCompatibleImage(image: BufferedImage): BufferedImage {
        val cm = image.colorModel
        return BufferedImage(
            cm,
            cm.createCompatibleWritableRaster(
                image.width,
                image.height
            ),
            cm.isAlphaPremultiplied, null
        )
    }

}