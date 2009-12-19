package org.cytoscape.layer.internal.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;

/**
 * The extended JList class to be able to reorder layer networks using drag and
 * drop.
 * 
 * @author kozo
 * 
 */
public class ReorderableJList extends JList implements DragSourceListener,
		DropTargetListener, DragGestureListener {

	static DataFlavor localObjectFlavor;
	static {
		try {
			localObjectFlavor = new DataFlavor(
					DataFlavor.javaJVMLocalObjectMimeType);
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
	}
	static DataFlavor[] supportedFlavors = { localObjectFlavor };
	DragSource dragSource;
	DropTarget dropTarget;
	Object dropTargetCell;
	int draggedIndex = -1;

	public ReorderableJList() {
		super();
		setCellRenderer(new ReorderableListCellRenderer());
		setModel(new DefaultListModel());
		dragSource = new DragSource();
		DragGestureRecognizer dgr = dragSource
				.createDefaultDragGestureRecognizer(this,
						DnDConstants.ACTION_MOVE, this);
		dropTarget = new DropTarget(this, this);
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
		System.out.println("dragDropEnd()");
		dropTargetCell = null;
		draggedIndex = -1;
		repaint();
	}

	public void dragEnter(DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub

	}

	public void dragExit(DragSourceEvent dse) {
		// TODO Auto-generated method stub

	}

	public void dragOver(DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub

	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
		// TODO Auto-generated method stub

	}

	public void dragEnter(DropTargetDragEvent dtde) {
		System.out.println("dragEnter");
		if (dtde.getSource() != dropTarget) {
			dtde.rejectDrag();
		} else {
			dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
			System.out.println("accepted dragEnter");
		}
	}

	public void dragExit(DropTargetEvent dte) {

	}

	public void dragOver(DropTargetDragEvent dtde) {
		if (dtde.getSource() != dropTarget) {
			dtde.rejectDrag();
		}
		Point dragPoint = dtde.getLocation();
		int index = locationToIndex(dragPoint);
		if (index == -1) {
			dropTargetCell = null;
		} else {
			dropTargetCell = getModel().getElementAt(index);
		}
		repaint();
	}

	public void drop(DropTargetDropEvent dtde) {
		System.out.println("drop()!");
		if (dtde.getSource() != dropTarget) {
			System.out.println("rejecting for bad source ("
					+ dtde.getSource().getClass().getName() + ")");
			dtde.rejectDrop();
			return;
		}
		Point dropPoint = dtde.getLocation();
		int index = locationToIndex(dropPoint);
		System.out.println("drop index is " + index);
		boolean dropped = false;
		try {
			if ((index == -1) || (index == draggedIndex)) {
				System.out.println("dropped onto self");
				dtde.rejectDrop();
				return;
			}
			dtde.acceptDrop(DnDConstants.ACTION_MOVE);
			System.out.println("accepted");
			Object dragged = dtde.getTransferable().getTransferData(
					localObjectFlavor);

			System.out.println("drop " + draggedIndex + " to " + index);
			boolean sourceBeforeTarget = (draggedIndex < index);
			System.out.println("source is" + (sourceBeforeTarget ? "" : " not")
					+ " before target");
			System.out.println("insert at "
					+ (sourceBeforeTarget ? index - 1 : index));
			DefaultListModel mod = (DefaultListModel) getModel();
			mod.remove(draggedIndex);
			mod.add((sourceBeforeTarget ? index - 1 : index), dragged);
			dropped = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		dtde.dropComplete(dropped);
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	public void dragGestureRecognized(DragGestureEvent dge) {
		System.out.println("dragGestureRecognized");
		Point clickPoint = dge.getDragOrigin();
		int index = locationToIndex(clickPoint);
		if (index == -1) {
			return;
		}
		Object target = getModel().getElementAt(index);
		Transferable trans = new RJLTransferable(target);
		draggedIndex = index;
		dragSource.startDrag(dge, Cursor.getDefaultCursor(), trans, this);
	}

	class RJLTransferable implements Transferable {
		Object object;

		public RJLTransferable(Object o) {
			object = o;
		}

		public Object getTransferData(DataFlavor df)
				throws UnsupportedFlavorException, IOException {
			if (isDataFlavorSupported(df)) {
				return object;
			} else {
				throw new UnsupportedFlavorException(df);
			}
		}

		public DataFlavor[] getTransferDataFlavors() {
			return supportedFlavors;
		}

		public boolean isDataFlavorSupported(DataFlavor df) {
			return (df.equals(localObjectFlavor));
		}
	}

	class ReorderableListCellRenderer extends DefaultListCellRenderer {
		boolean isTargetCell;
		boolean isLastItem;

		public ReorderableListCellRenderer() {
			super();
		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean hasFocus) {
			isTargetCell = (value == dropTargetCell);
			isLastItem = (index == list.getModel().getSize() - 1);
			boolean showSelected = isSelected & (dropTargetCell == null);
			return super.getListCellRendererComponent(list, value, index,
					showSelected, hasFocus);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (isTargetCell) {
				g.setColor(Color.black);
				g.drawLine(0, 0, getSize().width, 0);
			}
		}
	}

}
