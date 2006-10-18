package org.mindswap.swoop.utils.ui;

/**
 * @author from PhotoStuff
 *
 */

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mindswap.swoop.SwoopFrame;

public class LaunchBar extends JFrame implements ActionListener, WindowListener
{
    private static final Integer GOOGLE_IMAGE_SEARCH = new Integer(1);
    private static final Integer SWOOGLE_DOC_SEARCH = new Integer(2);
    private static final Integer SWOOGLE_TERMS_SEARCH = new Integer(3);
    private static final Integer SWOOGLE_CLASS_SEARCH = new Integer(4);
    private static final Integer SWOOGLE_PROPERTY_SEARCH = new Integer(5);

    private SwoopFrame swoopHandler;
    private JTextField mSearchText;
    private JComboBox mServiceComboBox;
    private JEditorPane resultPane;
    private Font tahoma = new Font("Tahoma", Font.PLAIN, 11);
    
    public LaunchBar(SwoopFrame handler)
    {
    	swoopHandler = handler;
        initGUI();
        this.addWindowListener(this);
    }

    public void actionPerformed(ActionEvent ev)
    {
        Pair selected = (Pair)mServiceComboBox.getSelectedItem();

        String searchString = mSearchText.getText();

        Integer id = (Integer)selected.mSecond;
        String url = null;

        if (id.equals(GOOGLE_IMAGE_SEARCH))
        {
            searchString = searchString.replace(' ','+');
            url = "http://images.google.com/images?q="+searchString+"&hl=en";
        }
        else if (id.equals(SWOOGLE_CLASS_SEARCH))
        {
            url = "http://pear.cs.umbc.edu/swoogle/modules.php?name=Swoogle_Search&file=termSearch&searchString="+searchString+"&start=1&searchClass=1";
        }
        else if (id.equals(SWOOGLE_PROPERTY_SEARCH))
        {
            url = "http://pear.cs.umbc.edu/swoogle/modules.php?name=Swoogle_Search&file=termSearch&searchString="+searchString+"&start=1&searchProperty=1";
        }
        else if (id.equals(SWOOGLE_DOC_SEARCH))
        {
            url = "http://pear.cs.umbc.edu/swoogle/modules.php?searchString="+searchString+"&start=1&total=-1&searchParam=&name=Swoogle_Search&file=searchDB";
        }
        else if (id.equals(SWOOGLE_TERMS_SEARCH))
        {
            url = "http://pear.cs.umbc.edu/swoogle/modules.php?name=Swoogle_Search&file=termSearch&searchString="+searchString+"&start=1&searchClass=1&searchProperty=1";
        }

        try {
            url = new java.net.URL(url).toString();
            BrowserControl.displayURL(url);
//            resultPane.setPage(url);
//            System.out.println(resultPane.getText());
            
            
        } // try
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initGUI()
    {
    	
    	Container content = this.getContentPane();
    	content.setLayout(new BorderLayout());
    	
    	// create query panel
    	final JButton search = new JButton("Go");
    	search.setFont(tahoma);
        search.addActionListener(this);

        mSearchText = new JTextField();

        KeyAdapter keyAdapter = new KeyAdapter() {
            public void keyReleased(KeyEvent ev) {
                if (ev.getKeyCode() == KeyEvent.VK_ENTER)
                    search.doClick();
            }
        };

        addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent ev) {
                mSearchText.requestFocus();
            }
        });

        mSearchText.addKeyListener(keyAdapter);
        search.addKeyListener(keyAdapter);

        makeServiceComboBox();

        JPanel queryPanel = new JPanel();
    	queryPanel.setLayout(new BorderLayout());
    	queryPanel.add(mServiceComboBox, "West");
    	queryPanel.add(mSearchText, "Center");
    	queryPanel.add(search, "East");
    	
    	resultPane = new JEditorPane();
        resultPane.setContentType("text/html");
        resultPane.setEditable(false);
        
    	content.add(queryPanel, "Center");
//        content.add(new JScrollPane(resultPane), "Center");
        
        setTitle("Launch Bar");
        setSize(400, 52);
        setLocation(50,50);
        setResizable(false);
    }

    private void makeServiceComboBox()
    {
        Vector list = new Vector();

        list.addElement(new Pair("Swoogle Terms Search",SWOOGLE_TERMS_SEARCH));
        list.addElement(new Pair("Swoogle Document Search",SWOOGLE_DOC_SEARCH));
        list.addElement(new Pair("Swoogle Class Search",SWOOGLE_CLASS_SEARCH));
        list.addElement(new Pair("Swoogle Property Search",SWOOGLE_PROPERTY_SEARCH));
        list.addElement(new Pair("Google Image Search",GOOGLE_IMAGE_SEARCH));
        
        mServiceComboBox = new JComboBox(list);

        mServiceComboBox.setFont(tahoma);
    }
    
    public class Pair {
    	  public Object mFirst;
    	  public Object mSecond;

    	  public Pair(Object f, Object s)
    	  {
    	    mFirst = f;
    	    mSecond = s;
    	  } // cons

    	  public String toString() { return mFirst.toString(); }
    	}

	public void windowOpened(WindowEvent arg0) {
	}

	public void windowClosing(WindowEvent arg0) {
		swoopHandler.launchBarMenu.setSelected(false);
	}

	public void windowClosed(WindowEvent arg0) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}

