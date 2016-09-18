import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This frame is made when the user clicks the notification button on the auction frame. It displays every 
 * notification in the message arraylist in auctionframe for the specific user as jlabels.
 * @author chloeallan
 *
 */
public class NotificationFrame extends JFrame{

	public NotificationFrame(String title){
		super(title);
		init();
	}

	public void init(){
		setSize(400,350);
		setMinimumSize(new Dimension(400,350));
		setLocationRelativeTo(null);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		add(panel);

		JLabel title = new JLabel("<html><center> Notifications: <br>");
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setFont(new Font(title.getFont().getFontName(), Font.BOLD, 14));
		panel.add(title);

		if(AuctionFrame.messages != null){
			for(String s : AuctionFrame.messages){
				System.out.println(s);
				JLabel message = new JLabel();
				message.setText(s);
				panel.add(message);
			}
		}

		setVisible(true);

	}

}
