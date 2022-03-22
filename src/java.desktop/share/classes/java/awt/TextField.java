/*
 * Copyright (c) 1995, 2021, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.peer.TextFieldPeer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.EventListener;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;

/**
 * A {@code TextField} object is a text component
 * that allows for the editing of a single line of text.
 * <p>
 * For example, the following image depicts a frame with four
 * text fields of varying widths. Two of these text fields
 * display the predefined text {@code "Hello"}.
 * <p>
 * <img src="doc-files/TextField-1.gif" alt="The preceding text describes this
 * image." style="margin: 7px 10px;">
 * <p>
 * Here is the code that produces these four text fields:
 *
 * <hr><blockquote><pre>
 * TextField tf1, tf2, tf3, tf4;
 * // a blank text field
 * tf1 = new TextField();
 * // blank field of 20 columns
 * tf2 = new TextField("", 20);
 * // predefined text displayed
 * tf3 = new TextField("Hello!");
 * // predefined text in 30 columns
 * tf4 = new TextField("Hello", 30);
 * </pre></blockquote><hr>
 * <p>
 * Every time the user types a key in the text field, one or
 * more key events are sent to the text field.  A {@code KeyEvent}
 * may be one of three types: keyPressed, keyReleased, or keyTyped.
 * The properties of a key event indicate which of these types
 * it is, as well as additional information about the event,
 * such as what modifiers are applied to the key event and the
 * time at which the event occurred.
 * <p>
 * The key event is passed to every {@code KeyListener}
 * or {@code KeyAdapter} object which registered to receive such
 * events using the component's {@code addKeyListener} method.
 * ({@code KeyAdapter} objects implement the
 * {@code KeyListener} interface.)
 * <p>
 * It is also possible to fire an {@code ActionEvent}.
 * If action events are enabled for the text field, they may
 * be fired by pressing the {@code Return} key.
 * <p>
 * The {@code TextField} class's {@code processEvent}
 * method examines the action event and passes it along to
 * {@code processActionEvent}. The latter method redirects the
 * event to any {@code ActionListener} objects that have
 * registered to receive action events generated by this
 * text field.
 *
 * @author      Sami Shaio
 * @see         java.awt.event.KeyEvent
 * @see         java.awt.event.KeyAdapter
 * @see         java.awt.event.KeyListener
 * @see         java.awt.event.ActionEvent
 * @see         java.awt.Component#addKeyListener
 * @see         java.awt.TextField#processEvent
 * @see         java.awt.TextField#processActionEvent
 * @see         java.awt.TextField#addActionListener
 * @since       1.0
 */
public class TextField extends TextComponent {

    /**
     * The number of columns in the text field.
     * A column is an approximate average character
     * width that is platform-dependent.
     * Guaranteed to be non-negative.
     *
     * @serial
     * @see #setColumns(int)
     * @see #getColumns()
     */
    int columns;

    /**
     * The echo character, which is used when
     * the user wishes to disguise the characters
     * typed into the text field.
     * The disguises are removed if echoChar = {@code 0}.
     *
     * @serial
     * @see #getEchoChar()
     * @see #setEchoChar(char)
     * @see #echoCharIsSet()
     */
    char echoChar;

    transient ActionListener actionListener;

    private static final String base = "textfield";
    private static int nameCounter = 0;

    /**
     * Use serialVersionUID from JDK 1.1 for interoperability.
     */
    @Serial
    private static final long serialVersionUID = -2966288784432217853L;

    /**
     * Initialize JNI field and method ids
     */
    private static native void initIDs();

    static {
        /* ensure that the necessary native libraries are loaded */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    /**
     * Constructs a new text field.
     * @throws HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public TextField() throws HeadlessException {
        this("", 0);
    }

    /**
     * Constructs a new text field initialized with the specified text.
     * @param      text       the text to be displayed. If
     *             {@code text} is {@code null}, the empty
     *             string {@code ""} will be displayed.
     *             If {@code text} contains EOL and/or LF characters, then
     *             each will be replaced by space character.
     * @throws HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public TextField(String text) throws HeadlessException {
        this(text, (text != null) ? text.length() : 0);
    }

    /**
     * Constructs a new empty text field with the specified number
     * of columns.  A column is an approximate average character
     * width that is platform-dependent.
     * @param      columns     the number of columns.  If
     *             {@code columns} is less than {@code 0},
     *             {@code columns} is set to {@code 0}.
     * @throws HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public TextField(int columns) throws HeadlessException {
        this("", columns);
    }

    /**
     * Constructs a new text field initialized with the specified text
     * to be displayed, and wide enough to hold the specified
     * number of columns. A column is an approximate average character
     * width that is platform-dependent.
     * @param      text       the text to be displayed. If
     *             {@code text} is {@code null}, the empty
     *             string {@code ""} will be displayed.
     *             If {@code text} contains EOL and/or LF characters, then
     *             each will be replaced by space character.
     * @param      columns     the number of columns.  If
     *             {@code columns} is less than {@code 0},
     *             {@code columns} is set to {@code 0}.
     * @throws HeadlessException if GraphicsEnvironment.isHeadless()
     * returns true.
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    public TextField(String text, int columns) throws HeadlessException {
        super(replaceEOL(text));
        this.columns = (columns >= 0) ? columns : 0;
    }

    /**
     * Construct a name for this component.  Called by getName() when the
     * name is null.
     */
    String constructComponentName() {
        synchronized (TextField.class) {
            return base + nameCounter++;
        }
    }

    /**
     * Creates the TextField's peer.  The peer allows us to modify the
     * appearance of the TextField without changing its functionality.
     */
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (peer == null)
                peer = getComponentFactory().createTextField(this);
            super.addNotify();
        }
    }

    /**
     * Gets the character that is to be used for echoing.
     * <p>
     * An echo character is useful for text fields where
     * user input should not be echoed to the screen, as in
     * the case of a text field for entering a password.
     * If {@code echoChar} = {@code 0}, user
     * input is echoed to the screen unchanged.
     * <p>
     * A Java platform implementation may support only a limited,
     * non-empty set of echo characters. This function returns the
     * echo character originally requested via setEchoChar(). The echo
     * character actually used by the TextField implementation might be
     * different.
     * @return      the echo character for this text field.
     * @see         java.awt.TextField#echoCharIsSet
     * @see         java.awt.TextField#setEchoChar
     */
    public char getEchoChar() {
        return echoChar;
    }

    /**
     * Sets the echo character for this text field.
     * <p>
     * An echo character is useful for text fields where
     * user input should not be echoed to the screen, as in
     * the case of a text field for entering a password.
     * Setting {@code echoChar} = {@code 0} allows
     * user input to be echoed to the screen again.
     * <p>
     * A Java platform implementation may support only a limited,
     * non-empty set of echo characters. Attempts to set an
     * unsupported echo character will cause the default echo
     * character to be used instead. Subsequent calls to getEchoChar()
     * will return the echo character originally requested. This might
     * or might not be identical to the echo character actually
     * used by the TextField implementation.
     * @param       c   the echo character for this text field.
     * @see         java.awt.TextField#echoCharIsSet
     * @see         java.awt.TextField#getEchoChar
     * @since       1.1
     */
    public void setEchoChar(char c) {
        setEchoCharacter(c);
    }

    /**
     * Sets the character to be echoed when protected input is displayed.
     *
     *  @param  c the echo character for this text field
     *
     * @deprecated As of JDK version 1.1,
     * replaced by {@code setEchoChar(char)}.
     */
    @Deprecated
    public synchronized void setEchoCharacter(char c) {
        if (echoChar != c) {
            echoChar = c;
            TextFieldPeer peer = (TextFieldPeer)this.peer;
            if (peer != null) {
                peer.setEchoChar(c);
            }
        }
    }

    /**
     * Sets the text that is presented by this
     * text component to be the specified text.
     * @param      t       the new text. If
     *             {@code t} is {@code null}, the empty
     *             string {@code ""} will be displayed.
     *             If {@code t} contains EOL and/or LF characters, then
     *             each will be replaced by space character.
     * @see         java.awt.TextComponent#getText
     */
    public void setText(String t) {
        super.setText(replaceEOL(t));

        // This could change the preferred size of the Component.
        invalidateIfValid();
    }

    /**
     * Replaces EOL characters from the text variable with a space character.
     * @param       text   the new text.
     * @return      Returns text after replacing EOL characters.
     */
    private static String replaceEOL(String text) {
        if (text == null) {
            return text;
        }
        String[] strEOLs = {System.lineSeparator(), "\n"};
        for (String eol : strEOLs) {
            if (text.contains(eol)) {
                text = text.replace(eol, " ");
            }
        }
        return text;
    }


    /**
     * Indicates whether or not this text field has a
     * character set for echoing.
     * <p>
     * An echo character is useful for text fields where
     * user input should not be echoed to the screen, as in
     * the case of a text field for entering a password.
     * @return     {@code true} if this text field has
     *                 a character set for echoing;
     *                 {@code false} otherwise.
     * @see        java.awt.TextField#setEchoChar
     * @see        java.awt.TextField#getEchoChar
     */
    public boolean echoCharIsSet() {
        return echoChar != 0;
    }

    /**
     * Gets the number of columns in this text field. A column is an
     * approximate average character width that is platform-dependent.
     * @return     the number of columns.
     * @see        java.awt.TextField#setColumns
     * @since      1.1
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Sets the number of columns in this text field. A column is an
     * approximate average character width that is platform-dependent.
     * @param      columns   the number of columns.
     * @see        java.awt.TextField#getColumns
     * @throws  IllegalArgumentException   if the value
     *                 supplied for {@code columns}
     *                 is less than {@code 0}.
     * @since      1.1
     */
    public void setColumns(int columns) {
        int oldVal;
        synchronized (this) {
            oldVal = this.columns;
            if (columns < 0) {
                throw new IllegalArgumentException("columns less than zero.");
            }
            if (columns != oldVal) {
                this.columns = columns;
            }
        }

        if (columns != oldVal) {
            invalidate();
        }
    }

    /**
     * Gets the preferred size of this text field
     * with the specified number of columns.
     * @param     columns the number of columns
     *                 in this text field.
     * @return    the preferred dimensions for
     *                 displaying this text field.
     * @since     1.1
     */
    public Dimension getPreferredSize(int columns) {
        return preferredSize(columns);
    }

    /**
     * Returns the preferred size for this text field
     * with the specified number of columns.
     *
     * @param  columns the number of columns
     * @return the preferred size for the text field
     *
     * @deprecated As of JDK version 1.1,
     * replaced by {@code getPreferredSize(int)}.
     */
    @Deprecated
    public Dimension preferredSize(int columns) {
        synchronized (getTreeLock()) {
            TextFieldPeer peer = (TextFieldPeer)this.peer;
            return (peer != null) ?
                       peer.getPreferredSize(columns) :
                       super.preferredSize();
        }
    }

    /**
     * Gets the preferred size of this text field.
     * @return     the preferred dimensions for
     *                         displaying this text field.
     * @since      1.1
     */
    public Dimension getPreferredSize() {
        return preferredSize();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by {@code getPreferredSize()}.
     */
    @Deprecated
    public Dimension preferredSize() {
        synchronized (getTreeLock()) {
            return (columns > 0) ?
                       preferredSize(columns) :
                       super.preferredSize();
        }
    }

    /**
     * Gets the minimum dimensions for a text field with
     * the specified number of columns.
     * @param  columns the number of columns in
     *         this text field.
     * @return the minimum size for this text field
     * @since    1.1
     */
    public Dimension getMinimumSize(int columns) {
        return minimumSize(columns);
    }

    /**
     * Returns the minimum dimensions for a text field with
     * the specified number of columns.
     *
     * @param  columns the number of columns
     * @return the minimum size for this text field
     * @deprecated As of JDK version 1.1,
     * replaced by {@code getMinimumSize(int)}.
     */
    @Deprecated
    public Dimension minimumSize(int columns) {
        synchronized (getTreeLock()) {
            TextFieldPeer peer = (TextFieldPeer)this.peer;
            return (peer != null) ?
                       peer.getMinimumSize(columns) :
                       super.minimumSize();
        }
    }

    /**
     * Gets the minimum dimensions for this text field.
     * @return     the minimum dimensions for
     *                  displaying this text field.
     * @since      1.1
     */
    public Dimension getMinimumSize() {
        return minimumSize();
    }

    /**
     * @deprecated As of JDK version 1.1,
     * replaced by {@code getMinimumSize()}.
     */
    @Deprecated
    public Dimension minimumSize() {
        synchronized (getTreeLock()) {
            return (columns > 0) ?
                       minimumSize(columns) :
                       super.minimumSize();
        }
    }

    /**
     * Adds the specified action listener to receive
     * action events from this text field.
     * If l is null, no exception is thrown and no action is performed.
     * <p>Refer to <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT Threading Issues</a> for details on AWT's threading model.
     *
     * @param      l the action listener.
     * @see        #removeActionListener
     * @see        #getActionListeners
     * @see        java.awt.event.ActionListener
     * @since      1.1
     */
    public synchronized void addActionListener(ActionListener l) {
        if (l == null) {
            return;
        }
        actionListener = AWTEventMulticaster.add(actionListener, l);
        newEventsOnly = true;
    }

    /**
     * Removes the specified action listener so that it no longer
     * receives action events from this text field.
     * If l is null, no exception is thrown and no action is performed.
     * <p>Refer to <a href="doc-files/AWTThreadIssues.html#ListenersThreads"
     * >AWT Threading Issues</a> for details on AWT's threading model.
     *
     * @param           l the action listener.
     * @see             #addActionListener
     * @see             #getActionListeners
     * @see             java.awt.event.ActionListener
     * @since           1.1
     */
    public synchronized void removeActionListener(ActionListener l) {
        if (l == null) {
            return;
        }
        actionListener = AWTEventMulticaster.remove(actionListener, l);
    }

    /**
     * Returns an array of all the action listeners
     * registered on this textfield.
     *
     * @return all of this textfield's {@code ActionListener}s
     *         or an empty array if no action
     *         listeners are currently registered
     *
     * @see #addActionListener
     * @see #removeActionListener
     * @see java.awt.event.ActionListener
     * @since 1.4
     */
    public synchronized ActionListener[] getActionListeners() {
        return getListeners(ActionListener.class);
    }

    /**
     * Returns an array of all the objects currently registered
     * as <code><em>Foo</em>Listener</code>s
     * upon this {@code TextField}.
     * <code><em>Foo</em>Listener</code>s are registered using the
     * <code>add<em>Foo</em>Listener</code> method.
     *
     * <p>
     * You can specify the {@code listenerType} argument
     * with a class literal, such as
     * <code><em>Foo</em>Listener.class</code>.
     * For example, you can query a
     * {@code TextField t}
     * for its action listeners with the following code:
     *
     * <pre>ActionListener[] als = (ActionListener[])(t.getListeners(ActionListener.class));</pre>
     *
     * If no such listeners exist, this method returns an empty array.
     *
     * @param listenerType the type of listeners requested; this parameter
     *          should specify an interface that descends from
     *          {@code java.util.EventListener}
     * @return an array of all objects registered as
     *          <code><em>Foo</em>Listener</code>s on this textfield,
     *          or an empty array if no such
     *          listeners have been added
     * @throws ClassCastException if {@code listenerType}
     *          doesn't specify a class or interface that implements
     *          {@code java.util.EventListener}
     *
     * @see #getActionListeners
     * @since 1.3
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        EventListener l = null;
        if  (listenerType == ActionListener.class) {
            l = actionListener;
        } else {
            return super.getListeners(listenerType);
        }
        return AWTEventMulticaster.getListeners(l, listenerType);
    }

    // REMIND: remove when filtering is done at lower level
    boolean eventEnabled(AWTEvent e) {
        if (e.id == ActionEvent.ACTION_PERFORMED) {
            if ((eventMask & AWTEvent.ACTION_EVENT_MASK) != 0 ||
                actionListener != null) {
                return true;
            }
            return false;
        }
        return super.eventEnabled(e);
    }

    /**
     * Processes events on this text field. If the event
     * is an instance of {@code ActionEvent},
     * it invokes the {@code processActionEvent}
     * method. Otherwise, it invokes {@code processEvent}
     * on the superclass.
     * <p>Note that if the event parameter is {@code null}
     * the behavior is unspecified and may result in an
     * exception.
     *
     * @param      e the event
     * @see        java.awt.event.ActionEvent
     * @see        java.awt.TextField#processActionEvent
     * @since      1.1
     */
    protected void processEvent(AWTEvent e) {
        if (e instanceof ActionEvent) {
            processActionEvent((ActionEvent)e);
            return;
        }
        super.processEvent(e);
    }

    /**
     * Processes action events occurring on this text field by
     * dispatching them to any registered
     * {@code ActionListener} objects.
     * <p>
     * This method is not called unless action events are
     * enabled for this component. Action events are enabled
     * when one of the following occurs:
     * <ul>
     * <li>An {@code ActionListener} object is registered
     * via {@code addActionListener}.
     * <li>Action events are enabled via {@code enableEvents}.
     * </ul>
     * <p>Note that if the event parameter is {@code null}
     * the behavior is unspecified and may result in an
     * exception.
     *
     * @param       e the action event
     * @see         java.awt.event.ActionListener
     * @see         java.awt.TextField#addActionListener
     * @see         java.awt.Component#enableEvents
     * @since       1.1
     */
    protected void processActionEvent(ActionEvent e) {
        ActionListener listener = actionListener;
        if (listener != null) {
            listener.actionPerformed(e);
        }
    }

    /**
     * Returns a string representing the state of this {@code TextField}.
     * This method is intended to be used only for debugging purposes, and the
     * content and format of the returned string may vary between
     * implementations. The returned string may be empty but may not be
     * {@code null}.
     *
     * @return      the parameter string of this text field
     */
    protected String paramString() {
        String str = super.paramString();
        if (echoChar != 0) {
            str += ",echo=" + echoChar;
        }
        return str;
    }


    /*
     * Serialization support.
     */
    /**
     * The textField Serialized Data Version.
     *
     * @serial
     */
    private int textFieldSerializedDataVersion = 1;

    /**
     * Writes default serializable fields to stream.  Writes
     * a list of serializable ActionListener(s) as optional data.
     * The non-serializable ActionListener(s) are detected and
     * no attempt is made to serialize them.
     *
     * @serialData Null terminated sequence of zero or more pairs.
     *             A pair consists of a String and Object.
     *             The String indicates the type of object and
     *             is one of the following :
     *             ActionListenerK indicating and ActionListener object.
     *
     * @param  s the {@code ObjectOutputStream} to write
     * @throws IOException if an I/O error occurs
     * @see AWTEventMulticaster#save(ObjectOutputStream, String, EventListener)
     * @see java.awt.Component#actionListenerK
     */
    @Serial
    private void writeObject(ObjectOutputStream s)
      throws IOException
    {
        s.defaultWriteObject();

        AWTEventMulticaster.save(s, actionListenerK, actionListener);
        s.writeObject(null);
    }

    /**
     * Read the ObjectInputStream and if it isn't null,
     * add a listener to receive action events fired by the
     * TextField.  Unrecognized keys or values will be
     * ignored.
     *
     * @param  s the {@code ObjectInputStream} to read
     * @throws ClassNotFoundException if the class of a serialized object could
     *         not be found
     * @throws IOException if an I/O error occurs
     * @throws HeadlessException if {@code GraphicsEnvironment.isHeadless()}
     *         returns {@code true}
     * @see #removeActionListener(ActionListener)
     * @see #addActionListener(ActionListener)
     * @see java.awt.GraphicsEnvironment#isHeadless
     */
    @Serial
    private void readObject(ObjectInputStream s)
      throws ClassNotFoundException, IOException, HeadlessException
    {
        // HeadlessException will be thrown by TextComponent's readObject
        s.defaultReadObject();
        text = replaceEOL(text);

        // Make sure the state we just read in for columns has legal values
        if (columns < 0) {
            columns = 0;
        }

        // Read in listeners, if any
        Object keyOrNull;
        while(null != (keyOrNull = s.readObject())) {
            String key = ((String)keyOrNull).intern();

            if (actionListenerK == key) {
                addActionListener((ActionListener)(s.readObject()));
            } else {
                // skip value for unrecognized key
                s.readObject();
            }
        }
    }


/////////////////
// Accessibility support
////////////////


    /**
     * Gets the AccessibleContext associated with this TextField.
     * For text fields, the AccessibleContext takes the form of an
     * AccessibleAWTTextField.
     * A new AccessibleAWTTextField instance is created if necessary.
     *
     * @return an AccessibleAWTTextField that serves as the
     *         AccessibleContext of this TextField
     * @since 1.3
     */
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleAWTTextField();
        }
        return accessibleContext;
    }

    /**
     * This class implements accessibility support for the
     * {@code TextField} class.  It provides an implementation of the
     * Java Accessibility API appropriate to text field user-interface elements.
     * @since 1.3
     */
    protected class AccessibleAWTTextField extends AccessibleAWTTextComponent
    {
        /**
         * Use serialVersionUID from JDK 1.3 for interoperability.
         */
        @Serial
        private static final long serialVersionUID = 6219164359235943158L;

        /**
         * Constructs an {@code AccessibleAWTTextField}.
         */
        protected AccessibleAWTTextField() {}

        /**
         * Gets the state set of this object.
         *
         * @return an instance of AccessibleStateSet describing the states
         * of the object
         * @see AccessibleState
         */
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            states.add(AccessibleState.SINGLE_LINE);
            return states;
        }
    }

}
