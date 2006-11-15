/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.bridge;

import java.awt.Cursor;

import org.apache.batik.dom.events.AbstractEvent;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLConstants;

import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.svg.SVGAElement;

/**
 * Bridge class for the &lt;a> element.
 *
 * @author <a href="mailto:tkormann@apache.org">Thierry Kormann</a>
 * @version $Id$
 */
public class SVGAElementBridge extends SVGGElementBridge {

    protected AnchorListener          al;
    protected CursorMouseOverListener bl;
    protected CursorMouseOutListener  cl;

    /**
     * Constructs a new bridge for the &lt;a> element.
     */
    public SVGAElementBridge() {}

    /**
     * Returns 'a'.
     */
    public String getLocalName() {
        return SVG_A_TAG;
    }

    /**
     * Returns a new instance of this bridge.
     */
    public Bridge getInstance() {
        return new SVGAElementBridge();
    }

    /**
     * Builds using the specified BridgeContext and element, the
     * specified graphics node.
     *
     * @param ctx the bridge context to use
     * @param e the element that describes the graphics node to build
     * @param node the graphics node to build
     */
    public void buildGraphicsNode(BridgeContext ctx,
                                  Element e,
                                  GraphicsNode node) {

        super.buildGraphicsNode(ctx, e, node);

        if (ctx.isInteractive()) {
            NodeEventTarget target = (NodeEventTarget)e;
            al = new AnchorListener(ctx.getUserAgent());
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_CLICK,
                 al, false, null);
            ctx.storeEventListenerNS
                (target, 
                 XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_CLICK,
                 al, false);

            bl = new CursorMouseOverListener(ctx.getUserAgent());
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOVER,
                 bl, false, null);
            ctx.storeEventListenerNS
                (target, 
                 XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOVER,
                 bl, false);
            cl = new CursorMouseOutListener(ctx.getUserAgent());
            target.addEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOUT,
                 cl, false, null);
            ctx.storeEventListenerNS
                (target, 
                 XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOUT,
                 cl, false);
        }
    }

    public void dispose() {
        NodeEventTarget target = (NodeEventTarget)e;
        if (al != null) {
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_CLICK, 
                 al, false);
            al = null;
        }
        if (bl != null) {
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOVER, 
                 bl, false);
            bl = null;
        }
        if (cl != null) {
            target.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, SVG_EVENT_MOUSEOUT, 
                 cl, false);
            cl = null;
        }
        super.dispose();
    }
    /**
     * Returns true as the &lt;a> element is a container.
     */
    public boolean isComposite() {
        return true;
    }

    /**
     * To handle a click on an anchor.
     */
    public static class AnchorListener implements EventListener {

        protected UserAgent userAgent;

        public AnchorListener(UserAgent ua) {
            userAgent = ua;
        }

        public void handleEvent(Event evt) {
            if (AbstractEvent.getEventPreventDefault(evt))
                return;
            SVGAElement elt = (SVGAElement)evt.getCurrentTarget();
            Cursor cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            userAgent.setSVGCursor(cursor);
            userAgent.openLink(elt);
            evt.stopPropagation();
        }
    }

    /**
     * To handle a mouseover on an anchor and set the cursor.
     */
    public static class CursorMouseOverListener implements EventListener {

        protected UserAgent userAgent;

        public CursorMouseOverListener(UserAgent ua) {
            userAgent = ua;
        }

        public void handleEvent(Event evt) {
            if (AbstractEvent.getEventPreventDefault(evt))
                return;
            //
            // Only modify the cursor if the target's cursor property is 
            // 'auto'. Note that we do not need to check the value of 
            // anchor element as the target's cursor value is resulting
            // from the CSS cascade which has accounted for inheritance.
            // This means that our behavior is to set the cursor to a 
            // hand cursor for any content on which the cursor property is
            // 'auto' inside an anchor element. If, for example, the 
            // content was:
            // <a cusor="wait">
            //    <g cursor="auto">
            //       <rect />
            //    </g>
            // </a>
            //
            // The cursor on the inside rect will be set to the hand cursor and
            // not the wait cursor
            //
            Element target = (Element)evt.getTarget();
            
            if (CSSUtilities.isAutoCursor(target)) {
                // The target's cursor value is 'auto': use the hand cursor
                userAgent.setSVGCursor(CursorManager.ANCHOR_CURSOR);
            }
            
            // 
            // In all cases, display the href in the userAgent
            //

            SVGAElement elt = (SVGAElement)evt.getCurrentTarget();
            if (elt != null) {
                String href = XLinkSupport.getXLinkHref(elt);
                userAgent.displayMessage(href);
            }
        }
    }

    /**
     * To handle a mouseout on an anchor and set the cursor.
     */
    public static class CursorMouseOutListener implements EventListener {

        protected UserAgent userAgent;

        public CursorMouseOutListener(UserAgent ua) {
            userAgent = ua;
        }

        public void handleEvent(Event evt) {
            if (AbstractEvent.getEventPreventDefault(evt))
                return;
            // No need to set the cursor on out events: this is taken care of
            // by the BridgeContext
            
            // Hide the href in the userAgent
            SVGAElement elt = (SVGAElement)evt.getCurrentTarget();
            if (elt != null) {
                userAgent.displayMessage("");
            }
        }
    }
}
