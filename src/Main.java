
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Main extends JFrame{
	public static void main(String[] args){
		new Main();
	}
	
	private JTextField startX;
	private JTextField startY;
	private JTextField endX;
	private JTextField endY;
	private JPanel config;
	private JPanel main;
	
	public Main(){
		
		startX = new JTextField(4);
		startY = new JTextField(4);
		endX = new JTextField(4);
		endY = new JTextField(4);
		
		getContentPane().setLayout(new BorderLayout());
		
		config = new JPanel();
		
		config.add(new JLabel("Start X"));
		
		config.add(startX);
		config.add(new JLabel("Start Y"));
		config.add(startY);
		config.add(new JLabel("End X"));
		config.add(endX);
		config.add(new JLabel("End Y"));
		config.add(endY);
		config.setLayout(new BoxLayout(config, BoxLayout.Y_AXIS));
		config.setMinimumSize(new Dimension(100,400));
		this.add(config, BorderLayout.WEST);
		
		main = new RobotPanel(this);
		main.setMinimumSize(new Dimension(400,400));
		this.add(main, BorderLayout.CENTER);

		this.setMinimumSize(new Dimension(500,400));
		setVisible(true);
		new Timer(100, new ActionListener(){
			public void actionPerformed(ActionEvent e){
				main.repaint();
			}
		}).start();
	}
	
	private int getIntValue(JTextField field){
		String s = field.getText();
		return Integer.parseInt(s);
	}
	
	public Point getStartPoint(){
		try{
			int x = getIntValue(startX);
			int y = getIntValue(startY);
			int x2 = getIntValue(endX);
			int y2 = getIntValue(endY);
			return new Point(x > x2 ? x2 : x ,y > y2 ? y2 : y);
		}
		catch(Exception e){
			throw new IllegalStateException("Invalid format in text fields");
		}
	}
	public Point getEndPoint(){
		try{
			int x = getIntValue(startX);
			int y = getIntValue(startY);
			int x2 = getIntValue(endX);
			int y2 = getIntValue(endY);
			return new Point(x > x2 ? x : x2 ,y > y2 ? y : y2);
		}
		catch(Exception e){
			throw new IllegalStateException("Invalid format in text fields");
		}
	}
	
	private class RobotPanel extends JPanel{
		Main view;
		private int step = 20;
		
		public RobotPanel(Main view){
			this.view = view;
		}
		
		@Override
		public void paint(Graphics g){
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, getWidth(), getHeight());
			
			g.setColor(Color.BLACK);
			
			Point startPoint, endPoint;
			try{
				startPoint = view.getStartPoint();
				endPoint = view.getEndPoint();
			}
			catch(IllegalStateException e){
				g.drawString("invalid input in config fields, please use integers", 50, 50);
				return;
			}
			// TODO known issue can't start at a higher x than end
			
			// dy/dx = -((b^2 * x) / (a^2 * sqrt(b^2 * (1 - (x^2/a^2))
			// where a = width and b = height
			double[] slopes = getAllSlopes(startPoint, endPoint);
			double[] expected = getExpectedYPositions(slopes, startPoint.x, endPoint.x);
			double[] actual = getActualYPositions(slopes, expected, 0.5, 0.3, 0.2, 0.1);
			
			for(int x = 0; x < expected.length-1; x++){
				if(x % 10 > 5){
					g.drawLine(startPoint.x + x, startPoint.y + (int)expected[x], startPoint.x + x + 1, startPoint.y + (int)expected[x+1]);
				}
			}
			g.setColor(Color.RED);
			for(int x = 0; x < actual.length-1; x++){
				g.drawLine(startPoint.x + x*step, startPoint.y + (int)actual[x], startPoint.x + x*step + step, startPoint.y + (int)actual[x+1]);
			}
		}
		
		private double[] getExpectedYPositions(double[] slopes, int startX, int startY){
			double[] expected = new double[slopes.length];
			double y = 0;
			for(int x = 0; x < slopes.length; x++){
				y += slopes[x];
				expected[x] = y;
			}
			
			return expected;
		}
		
		private double[] getAllSlopes(Point startPoint, Point endPoint){
			int width = endPoint.x - startPoint.x;
			int height = endPoint.y - startPoint.y;
			double[] expected = new double[width];
			for(int x = 0; x < width; x++)
				expected[x] = getSlope(width, height, x);
			return expected;
		}
		
		private double getSlope(int width, int height, int x){
			return -( ( Math.pow(height, 2) * x ) / ( Math.pow(width, 2) * Math.sqrt(Math.pow(height, 2) * ( 1 - (Math.pow(x, 2)/Math.pow(width, 2) ) ) ) ) );
		}
		
		private double[] getActualYPositions(double[] slopes, double[] expected, double variation, double kP, double kD, double kI){
			double dComp = 0;
			double iComp = 0;
			
			double y = 0;
			double[] actual = new double[slopes.length/step];
			
			for(int x = 0; x < slopes.length-1; x+=step){
				double change = step * slopes[x] * ((1-variation) + Math.random()*variation);
				y += change;
				
				double err = expected[x] - y;
				
				// PID
				y += err * kP;
				y += dComp * kD;
				y += iComp * kI;
				
				dComp = err;
				iComp += err;
						
				actual[x/step] = y;
			}
			
			return actual;
		}
	}
}
