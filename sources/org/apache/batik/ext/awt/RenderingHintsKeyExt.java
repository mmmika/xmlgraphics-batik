/*

   Copyright 2001-2002  The Apache Software Foundation 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.ext.awt;

import java.awt.RenderingHints;

/**
 * Contains additional RenderingHints Keys, such as 
 * KEY_AREA_OF_INTEREST
 *
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id$
 */
public final class RenderingHintsKeyExt {
    public static final int KEY_BASE;

    /**
     * Hint as to the transcoding destination.
     */
    public static final RenderingHints.Key KEY_TRANSCODING;

    public static final String VALUE_TRANSCODING_PRINTING = 
        new String("Printing");
    
    /**
     * Key for the AOI hint. This hint is used to propagate the AOI to Paint
     * and PaintContext instances.
     */
    public static final RenderingHints.Key KEY_AREA_OF_INTEREST;

    /**
     * Hint for the destination of the rendering when it is a BufferedImage
     * This works around the fact that Java 2D sometimes lies about the
     * attributes of the Graphics2D device, when it is an image.
     *
     * It is strongly suggested that you use
     * org.apache.batik.ext.awt.image.GraphicsUtil.createGraphics to
     * create a Graphics2D from a BufferedImage, this will ensure that
     * the proper things are done in the processes of creating the
     * Graphics.  */
    public static final RenderingHints.Key KEY_BUFFERED_IMAGE;

    /**
     * Hint to source that we only want an alpha channel.
     * The source should follow the SVG spec for how to
     * convert ARGB, RGB, Grey and AGrey to just an Alpha channel.
     */
    public static final RenderingHints.Key KEY_COLORSPACE;

    static {
        int base = 10100;
        RenderingHints.Key trans=null, aoi=null, bi=null, cs=null;
        while (true) {
            int val = base;

            try {
                trans = new TranscodingHintKey   (val++);
                aoi   = new AreaOfInterestHintKey(val++);
                bi    = new BufferedImageHintKey (val++);
                cs    = new ColorSpaceHintKey    (val++);
            } catch (Exception e) {
                System.err.println
                    ("You have loaded the Batik jar files more than once\n" +
                     "in the same JVM this is likely a problem with the\n" +
                     "way you are loading the Batik jar files.");
                
                base = (int)(Math.random()*2000000);
                continue;
            }
            break;
        }
        KEY_BASE             = base;
        KEY_TRANSCODING      = trans;
        KEY_AREA_OF_INTEREST = aoi;
        KEY_BUFFERED_IMAGE   = bi;
        KEY_COLORSPACE       = cs;
    }

    /**
     * Do not authorize creation of instances of that class
     */
    private RenderingHintsKeyExt(){
    }
}
