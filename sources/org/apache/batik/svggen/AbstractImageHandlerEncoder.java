/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included with this distribution in  *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.batik.svggen;

import java.awt.*;
import java.awt.image.*;
import java.awt.image.renderable.RenderableImage;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.net.*;

import org.w3c.dom.Element;

import org.apache.batik.dom.util.XLinkSupport;

/**
 * This abstract implementation of the ImageHandler interface
 * is intended to be the base class for ImageHandlers that generate
 * image files for all the images they handle. This class stores
 * images in an configurable directory. The xlink:href value the
 * class generates is made of a configurable url root and the name
 * of the file created by this handler.
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id$
 * @see             org.apache.batik.svggen.SVGGraphics2D
 * @see             org.apache.batik.svggen.ImageHandlerJPEGEncoder
 * @see             org.apache.batik.svggen.ImageHandlerPNGEncoder
 */
public abstract class AbstractImageHandlerEncoder extends DefaultImageHandler{
    static final String ERROR_NULL_INPUT = "imageDir should not be null";
    static final String ERROR_IMAGE_DIR_DOES_NOT_EXIST = "imageDir does not exist";
    static final String ERROR_CANNOT_USE_IMAGE_DIR_AS_URL = "cannot convert imageDir to a URL value : ";

    static final AffineTransform IDENTITY = new AffineTransform();

    /**
     * Directory where all images are placed
     */
    private String imageDir = "";

    /**
     * Value for the url root corresponding to the directory
     */
    private String urlRoot = "";

    /**
     * @param imageDir directory where this handler should generate images.
     *        If null, an IllegalArgumentException is thrown.
     * @param urlRoot root for the urls that point to images created by this
     *        image handler. If null, then the url corresponding to imageDir
     *        is used.
     */
    public AbstractImageHandlerEncoder(String imageDir, String urlRoot){
        if(imageDir == null)
            throw new IllegalArgumentException(ERROR_NULL_INPUT);

        File imageDirFile = new File(imageDir);
        if(!imageDirFile.exists())
            throw new IllegalArgumentException(ERROR_IMAGE_DIR_DOES_NOT_EXIST);

        this.imageDir = imageDir;
        if(urlRoot != null)
            this.urlRoot = urlRoot;
        else{
            try{
                this.urlRoot = imageDirFile.toURL().toString();
            }catch(MalformedURLException e){
                throw new IllegalArgumentException(ERROR_CANNOT_USE_IMAGE_DIR_AS_URL + e.getMessage());
            }
        }
    }

    /**
     * This template method should set the xlink:href attribute on the input
     * Element parameter
     */
    protected void handleHREF(Image image, Element imageElement){
        // Create an buffered image where the image will be drawn
        Dimension size = new Dimension(image.getWidth(null), image.getHeight(null));
        BufferedImage buf = buildBufferedImage(size);
        java.awt.Graphics2D g = buf.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        // Save image into file
        saveBufferedImageToFile(imageElement, buf);
    }

    /**
     * This template method should set the xlink:href attribute on the input
     * Element parameter
     */
    protected void handleHREF(RenderedImage image, Element imageElement){
        // Create an buffered image where the image will be drawn
        Dimension size = new Dimension(image.getWidth(), image.getHeight());
        BufferedImage buf = buildBufferedImage(size);
        java.awt.Graphics2D g = buf.createGraphics();
        g.drawRenderedImage(image, IDENTITY);
        g.dispose();

        // Save image into file
        saveBufferedImageToFile(imageElement, buf);
    }

    /**
     * This template method should set the xlink:href attribute on the input
     * Element parameter
     */
    protected void handleHREF(RenderableImage image, Element imageElement){
        // Create an buffered image where the image will be drawn
        Dimension size = new Dimension((int)Math.ceil(image.getWidth()),
                                       (int)Math.ceil(image.getHeight()));
        BufferedImage buf = buildBufferedImage(size);
        java.awt.Graphics2D g = buf.createGraphics();
        g.drawRenderableImage(image, IDENTITY);
        g.dispose();

        // Save image into file
        saveBufferedImageToFile(imageElement, buf);
    }

    private void saveBufferedImageToFile(Element imageElement, BufferedImage buf){
        // Create a new file in image directory
        File imageFile = null;

        // While the files we are generating exist, try to create another
        // id that is unique.
        while(imageFile == null){
            String fileId = SVGIDGenerator.generateID(getPrefix());
            imageFile = new File(imageDir, fileId + getSuffix());
            if(imageFile.exists())
                imageFile = null;
        }

        // Encode image here
        encodeImage(buf, imageFile);

        // Update HREF
        imageElement.setAttributeNS(XLinkSupport.XLINK_NAMESPACE_URI, ATTR_XLINK_HREF, urlRoot + "/" + imageFile.getName());
    }

    /**
     * @return the suffix used by this encoder. E.g., ".jpg" for ImageHandlerJPEGEncoder
     */
    public abstract String getSuffix();

    /**
     * @return the prefix used by this encoder. E.g., "jpegImage" for ImageHandlerJPEGEncoder
     */
    public abstract String getPrefix();

    /**
     * Derived classes should implement this method and encode the input
     * BufferedImage as needed
     */
    public abstract void encodeImage(BufferedImage buf, File imageFile);

    /**
     * This method creates a BufferedImage of the right size and type
     * for the derived class.
     */
    public abstract BufferedImage buildBufferedImage(Dimension size);
}
