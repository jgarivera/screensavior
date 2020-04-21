package me.jg.ss.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class Main {

	public static void main(String[] args) {
		new Main();
	}

	public Main() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException ex) {
				}

				JFrame frame = new JFrame("ScreenSavior");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setLayout(new BorderLayout());
				ConsolePane console = new ConsolePane();

				frame.add(new DropPane(console));
				frame.add(console, BorderLayout.PAGE_END);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}

	@SuppressWarnings("serial")
	public class ConsolePane extends JPanel {
		private JTextArea console;

		public ConsolePane() {
			console = new JTextArea(8, 58);
			console.setEditable(false);
			console.setFont(console.getFont().deriveFont(Font.ITALIC, 12));
			setBorder(new TitledBorder(new EtchedBorder(), "Console"));

			JScrollPane scroll = new JScrollPane(console);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

			add(scroll);
		}

		public void log(String str) {
			console.append(str + "\n");
		}
	}

	@SuppressWarnings("serial")
	public class DropPane extends JPanel {

		private DropTarget dropTarget;
		private DropTargetHandler dropTargetHandler;
		private Point dragPoint;
		private List<File> files;
		private ArrayList<BufferedImage> images = new ArrayList<>();
		private String exportDir = "D:/Users/Gab/Desktop/Kindle-Scaled";
		private boolean dragOver = false;
		private BufferedImage target;

		private JLabel message, indexTitle;
		private JTextField startInput;
		private JButton clearBtn, processBtn;
		private ConsolePane console;
		private int startIndex;

		public DropPane(ConsolePane console) {
			this.console = console;

			setLayout(new GridBagLayout());
			message = new JLabel();
			message.setFont(message.getFont().deriveFont(Font.BOLD, 18));
			message.setText("Drop images \n");
			add(message);

			indexTitle = new JLabel("Start index: \n");
			add(indexTitle);

			startInput = new JTextField(1);
			startInput.setEditable(true);
			startInput.addKeyListener(new KeyAdapter() {
				public void keyTyped(KeyEvent e) {
					if (startInput.getText().length() >= 2)
						e.consume();
				}
			});
			add(startInput);

			processBtn = new JButton("\n Process");
			processBtn.addActionListener((e) -> process());

			add(processBtn);

			clearBtn = new JButton("Clear");
			clearBtn.addActionListener((e) -> clear());

			// add(clearBtn);

		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(400, 400);
		}

		protected DropTarget getMyDropTarget() {
			if (dropTarget == null) {
				dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, null);
			}
			return dropTarget;
		}

		protected DropTargetHandler getDropTargetHandler() {
			if (dropTargetHandler == null) {
				dropTargetHandler = new DropTargetHandler();
			}
			return dropTargetHandler;
		}

		@Override
		public void addNotify() {
			super.addNotify();
			try {
				getMyDropTarget().addDropTargetListener(getDropTargetHandler());
			} catch (TooManyListenersException ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void removeNotify() {
			super.removeNotify();
			getMyDropTarget().removeDropTargetListener(getDropTargetHandler());
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (dragOver) {
				Graphics2D g2d = (Graphics2D) g.create();
				g2d.setColor(new Color(0, 255, 0, 64));
				g2d.fill(new Rectangle(getWidth(), getHeight()));
				if (dragPoint != null && target != null) {
					int x = dragPoint.x - 12;
					int y = dragPoint.y - 12;
					g2d.drawImage(target, x, y, this);
				}
				g2d.dispose();
			}
		}

		protected void importFiles(final List<File> files) {
			this.files = files;
			Runnable run = new Runnable() {
				@Override
				public void run() {
					message.setText("You dropped " + files.size() + " file(s)! \n");
				}
			};
			SwingUtilities.invokeLater(run);
		}

		protected void clear() {
			if (files != null)
				files.clear();
			if (images != null)
				images.clear();
			message.setText("Drop images \n");
		}

		protected void process() {

			if (files == null) {
				JOptionPane.showMessageDialog(null, "Please drop some images first.");
				return;
			}
			if (startInput.getText().length() <= 0) {
				JOptionPane.showMessageDialog(null, "Please input start index.");
				return;
			}

			startIndex = Integer.parseInt(startInput.getText());
			console.log("Start index is at " + startIndex);

			int imgCount = 0;
			long start = System.currentTimeMillis();

			for (File f : files) {
				if (f.getName().endsWith(".jpg") || f.getName().endsWith(".png") || f.getName().endsWith(".JPG")
						|| f.getName().endsWith(".PNG") || f.getName().endsWith(".JPEG")) {
					try {
						images.add(ImageIO.read(f));
						console.log("Imported " + f.getName());
						imgCount++;
					} catch (IOException e) {
						console.log("Error in reading " + f.getName());
					}
				}
			}

			console.log("Imported " + imgCount + " images.");
			console.log("Scaling images to 1072x1448 resolution...");

			imgCount = 0;
			for (BufferedImage i : images) {
				BufferedImage scaled = convertToBufferedImage(i.getScaledInstance(1072, 1448, Image.SCALE_SMOOTH));
				imgCount++;
				console.log("Successfully scaled image " + imgCount);

				String name = "";

				if (startIndex < 10)
					name = name.concat("bg_ss0" + startIndex + ".png");
				else
					name = name.concat("bg_ss" + startIndex + ".png");

				try {
					ImageIO.write(scaled, "png", new File(exportDir + "/", name));
					console.log("Exported to " + exportDir);
					startIndex++;
					console.log("Moving to index " + startIndex);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			console.log("Done! (" + (System.currentTimeMillis() - start) + " ms)");
			message.setText("Done!");
		}

		public BufferedImage convertToBufferedImage(Image image) {
			BufferedImage newImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
					BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = newImage.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
			return newImage;
		}

		protected class DropTargetHandler implements DropTargetListener {

			protected void processDrag(DropTargetDragEvent dtde) {
				if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.acceptDrag(DnDConstants.ACTION_COPY);
				} else {
					dtde.rejectDrag();
				}
			}

			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
				processDrag(dtde);
				SwingUtilities.invokeLater(new DragUpdate(true, dtde.getLocation()));
				repaint();
			}

			@Override
			public void dragOver(DropTargetDragEvent dtde) {
				processDrag(dtde);
				SwingUtilities.invokeLater(new DragUpdate(true, dtde.getLocation()));
				repaint();
			}

			@Override
			public void dropActionChanged(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragExit(DropTargetEvent dte) {
				SwingUtilities.invokeLater(new DragUpdate(false, null));
				repaint();
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void drop(DropTargetDropEvent dtde) {

				SwingUtilities.invokeLater(new DragUpdate(false, null));

				Transferable transferable = dtde.getTransferable();
				if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.acceptDrop(dtde.getDropAction());
					try {

						List transferData = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
						if (transferData != null && transferData.size() > 0) {
							importFiles(transferData);
							dtde.dropComplete(true);
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					dtde.rejectDrop();
				}
			}
		}

		public class DragUpdate implements Runnable {

			private boolean dragOver;
			private Point dragPoint;

			public DragUpdate(boolean dragOver, Point dragPoint) {
				this.dragOver = dragOver;
				this.dragPoint = dragPoint;
			}

			@Override
			public void run() {
				DropPane.this.dragOver = dragOver;
				DropPane.this.dragPoint = dragPoint;
				DropPane.this.repaint();
			}
		}
	}
}