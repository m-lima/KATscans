package no.uib.inf252.katscan.view.opengl;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;

/**
 *
 * @author Marcelo Lima
 */
public class SingleTriangle extends GLJPanel implements GLEventListener {

    private int width;
    private int height;

    public SingleTriangle() throws GLException {
        super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        addGLEventListener(this);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl2 = drawable.getGL().getGL2();
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();

        // coordinate system origin at lower left with width and height same as the window
        GLU glu = new GLU();
        glu.gluOrtho2D(0.0f, getWidth(), 0.0f, getHeight());

        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity();

        gl2.glViewport(0, 0, getWidth(), getHeight());
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl2 = drawable.getGL().getGL2();
        gl2.glClear(GL.GL_COLOR_BUFFER_BIT);

        // draw a triangle filling the window
        gl2.glLoadIdentity();
        gl2.glBegin(GL.GL_TRIANGLES);
        gl2.glColor3f(1, 0, 0);
        gl2.glVertex2f(0, 0);
        gl2.glColor3f(0, 1, 0);
        gl2.glVertex2f(width, 0);
        gl2.glColor3f(0, 0, 1);
        gl2.glVertex2f(width / 2, height);
        gl2.glEnd();

        drawable.swapBuffers();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.width = width;
        this.height = height;
        GL2 gl2 = drawable.getGL().getGL2();
        
        gl2.glMatrixMode( GL2.GL_PROJECTION );
        gl2.glLoadIdentity();

        // coordinate system origin at lower left with width and height same as the window
        GLU glu = new GLU();
        glu.gluOrtho2D( 0.0f, width, 0.0f, height );

        gl2.glMatrixMode( GL2.GL_MODELVIEW );
        gl2.glLoadIdentity();

        gl2.glViewport( 0, 0, width, height );
    }

}
