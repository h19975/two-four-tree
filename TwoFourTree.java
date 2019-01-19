package comp2402a10;
import java.util.ArrayList;
import java.util.Arrays;
/**
 * The TwoFourTree class is an implementation of the 2-4 tree from notes/textbook.
 * The tree will store Strings as values. 
 * It extends the (modified) sorted set interface (for strings).
 * It implements the LevelOrderTraversal interface. 
 */
public class TwoFourTree extends StringSSet implements LevelOrderTraversal{

		/* your class MUST have a zero argument constructor. All testing will 
		   use this ocnstructor to create a 2-3 tree.
    */
		private Node root;
		private int numOfNodes;
		private Node nodeToAdd;
		private String findValue;

		public TwoFourTree(){
			root = new Node();
			numOfNodes = 0;
			nodeToAdd = root;
			findValue = null;
		}

		@Override
		public int size() {
			return numOfNodes;
		}

		@Override
		public String find(String x) {
			return findValue(x, root, null);
		}
			

		private String findValue(String x, Node node, String value) {
			Node current = node;
			if (node == StringSSet.NIL) {
				return (value == null || value.compareTo(x) < 0) ? null : value;
			}

			for (int i = 0; i < 3; i++) {
				if (current.data[i] != StringSSet.EMPTY && current.data[i].compareTo(x) == 0) {
					return current.data[i];
				} else if (current.data[i] != StringSSet.EMPTY && x.compareTo(current.data[i]) < 0) {
					value = current.data[i];
					return findValue(x, current.children[i], value);
				} else if (i == numOfData(node)-1 && current.data[i] != StringSSet.EMPTY && x.compareTo(node.data[i]) > 0) {
					return findValue(x, current.children[i+1], value);
				}
			}

			return null;

		}

		@Override
		public boolean add(String x) {
			if (nodeContainString(root, x) != null) {
				return false;
			}
			addToNode(x, StringSSet.NIL, nodeToAdd);
			numOfNodes++;
			return true;
		}

		private void addToNode(String x, Node toBeAdd, Node node) {
			if (toBeAdd != StringSSet.NIL) {
				toBeAdd.parent = node;
			}

			// when there is space inside node
			if (node.data[2] == StringSSet.EMPTY) {
				insertData(x, toBeAdd, node);
				return;
			}

			// when there is no space inside node, split
			String [] newData = new String[4];
			Node [] newChildren = new Node [5];
			newChildren[0] = node.children[0];
			int i = 0;
			for (; i < 3 && node.data[i].compareTo(x) < 0; i++) {
				newData[i] = node.data[i];
				newChildren[i+1] = node.children[i+1];
			}

			// create virtual data and children
			newData[i] = x;
			newChildren[i+1] = toBeAdd;

			for (; i < 3; i++) {
				newData[i+1] = node.data[i];
				newChildren[i+2] = node.children[i+1];
			}

			// split to left subtree and right subtree
			for (int j = 0; j < 2; j++) {
				node.data[j] = newData[j];
			}
			for (int j = 0; j < 3; j++) {
				node.children[j] = newChildren[j];
			}

			node.data[2] = StringSSet.EMPTY;
			node.children[3] = StringSSet.NIL;
			String temp = newData[2];

			Node rightNode = new Node();
			rightNode.data[0] = newData[3];
			rightNode.children[0] = newChildren[3];
			rightNode.children[1] = newChildren[4];
			// update parents
			if (rightNode.children[0] != StringSSet.NIL) {
				rightNode.children[0].parent = rightNode;
			}
			if (rightNode.children[1] != StringSSet.NIL) {
				rightNode.children[1].parent = rightNode;
			}

			if (node == root) {
				root = new Node();
				root.data[0] = temp;
				root.children[0] = node;
				root.children[1] = rightNode;
				node.parent = root;
				rightNode.parent = root;
			} else {
				addToNode(temp, rightNode, node.parent);
			}
		}

		private Node nodeContainString(Node node, String x) {
			if (node == StringSSet.NIL) {
				return null;
			}

			nodeToAdd = node;
			for (int i = 0; i < 3; i++) {
				if (node.data[i] != StringSSet.EMPTY && x.compareTo(node.data[i]) < 0) {
					return nodeContainString(node.children[i], x);
				} else if (node.data[i] != StringSSet.EMPTY && x.compareTo(node.data[i]) == 0) {
					return node;
				} else if (i == numOfData(node)-1 && x.compareTo(node.data[i]) > 0) {
					return nodeContainString(node.children[i+1], x);
				}
			}

			return null;
		}

		@Override
		public boolean remove(String x) {
			if (numOfNodes == 0) {
				return false;
			}

			Node node = nodeContainString(root, x);
			if (node == null) {
				return false;
			}

			int index = 0;
			for (; index < 3; index++) {
				if (node.data[index] == x) {
					break;
				}
			}

			// if node is a leaf, remove x
			if (node.children[0] == StringSSet.NIL) {
				for (int i = index; i < 2; i++) {
					node.data[i] = node.data[i+1];
				}
				node.data[2] = StringSSet.EMPTY;
			// node is not a leaf, replace with predecessor and delete
			} else {
				Node predecessor = node.children[index];
				while (lastChild(predecessor) != StringSSet.NIL) {
					predecessor = lastChild(predecessor);
				}
				node.data[index] = predecessor.data[numOfData(predecessor)-1];
				predecessor.data[numOfData(predecessor)-1] = StringSSet.EMPTY;
				node = predecessor;
			}

			// resolve underflow situation
			if (node.data[0] == StringSSet.EMPTY) {
				resolveUnderFlow(node, StringSSet.NIL);
			}
			numOfNodes -- ;
			return true;
		}

		private void resolveUnderFlow(Node node, Node toBeAdd) {
			// nothing we can do if the root contains no data
			if (node == root) {
				root = toBeAdd;
				return;
			}

			Node parent = node.parent;
			int index = 0;
			
			for (; index < 4; index ++) {
				if (parent.children[index] == node) {
					break;
				}
			}

			// find which sibling to transfer
			int leftDataNum = index > 0 ? numOfData(parent.children[index-1]) : -1;
			int rightDataNum = index < 3 ? numOfData(parent.children[index+1]) : -1;

			// transfer from left sibling
			if (leftDataNum >= rightDataNum && leftDataNum > 1) {
				Node leftSibling = parent.children[index-1];
				int lastLeftDataIndex = leftDataNum - 1;
				// transfer leftsibling subtree to node
				node.data[0] = parent.data[index-1];
				node.children[0] = leftSibling.children[lastLeftDataIndex+1];
				// update new child's parent
				if (node.children[0] != StringSSet.NIL) {
					node.children[0].parent = node;
				}
				node.children[1] = toBeAdd;
				if (toBeAdd != StringSSet.NIL) {
					toBeAdd.parent = node;
				}
				parent.data[index-1] = leftSibling.data[lastLeftDataIndex];
				leftSibling.data[lastLeftDataIndex] = StringSSet.EMPTY;
				leftSibling.children[lastLeftDataIndex+1] = StringSSet.NIL;
				return;

			// transfer from right sibling
			} else if (rightDataNum > leftDataNum  && rightDataNum  > 1) {
				Node rightSibling = parent.children[index+1];
				node.children[0] = toBeAdd;
				if (toBeAdd != StringSSet.NIL) {
					toBeAdd.parent = node;
				}

				node.data[0] = parent.data[index];
				node.children[1] = rightSibling.children[0];
				if (node.children[1] != StringSSet.NIL) {
					node.children[1].parent = node;
				}

				// transfer data and children from right sibling
				parent.data[index] = rightSibling.data[0];

				// update right sibling
				for (int i = 0; i < 2; i++) {
					rightSibling.data[i] = rightSibling.data[i+1];
				}

				for (int i = 0; i < 3; i++) {
					rightSibling.children[i] = rightSibling.children[i+1];
				}

				rightSibling.data[2] = StringSSet.EMPTY;
				rightSibling.children[3] = StringSSet.NIL;
				return;
			}

			// perform fuse
			// perform left fuse
			if (index != 0 && parent.children[index-1] != StringSSet.NIL) {
				Node lefeSibling = parent.children[index-1];
				// transfer parent entry to the second element
				lefeSibling.data[1] = parent.data[index-1];
				lefeSibling.children[2] = toBeAdd;
				if (toBeAdd != StringSSet.NIL) {
					toBeAdd.parent = lefeSibling;
				}
				for (int i = index-1; i < 2; i++) {
					parent.data[i] = parent.data[i+1];
					parent.children[i+1] = parent.children[i+2];
				}
				parent.data[2] = StringSSet.EMPTY;
				parent.children[3] = StringSSet.NIL;
				if (parent.data[0] == StringSSet.EMPTY) {
					resolveUnderFlow(parent, lefeSibling);
				}
			} else if (index != 3 && parent.children[index+1] != StringSSet.NIL) {
				Node rightSibling = parent.children[index+1];
				//shift children and datas for rightSibling
				rightSibling.data[1] = rightSibling.data[0];
				rightSibling.data[0] = parent.data[index];
				rightSibling.children[2] = rightSibling.children[1];
				rightSibling.children[1] = rightSibling.children[0];
				rightSibling.children[0] = toBeAdd;
				if (toBeAdd != StringSSet.NIL) {
					toBeAdd.parent = rightSibling;
				}

				for (int i = index; i < 2; i++) {
					parent.data[i] = parent.data[i+1];
					parent.children[i] = parent.children[i+1];
				}

				parent.data[2] = StringSSet.EMPTY;
				parent.children[2] = parent.children[3];
				parent.children[3] = StringSSet.NIL;
				if (parent.data[0] == StringSSet.EMPTY) {
					resolveUnderFlow(parent, rightSibling);
				}
			}
		}

		@Override
		public void clear() {
			root = null;
			numOfNodes = 0;
			nodeToAdd = null;
		}

		public String levelOrder() {
			if (numOfNodes == 0) {
				return "";
			}
			Node node = root;
			String ret = "";
			ArrayList<Node> al = new ArrayList<Node>();
			al.add(node);
			ArrayList<Node> newAl = new ArrayList<Node>();
			while (al.size() > 0) {
				if (al.get(0) != root) {
					ret += "|";
				}
				for (int i = 0; i < al.size(); i++) {
					if (i != 0) {
						ret += ":";
					}
					ret += nodeToString(al.get(i));
					for (int j = 0; j < 4; j++) {
						if (al.get(i).children[j] != StringSSet.NIL) {
							newAl.add(al.get(i).children[j]);
						}
					}
				}
				al.clear();
				al = new ArrayList<Node>(newAl);
				newAl.clear();
			}
			return ret;
		}

	private void insertData(String x, Node tobeAdd, Node node) {
		int i = 0;
		for ( ; i < 2; i++) {
			if (node.data[i] ==  StringSSet.EMPTY || x.compareTo(node.data[i]) < 0) {
				break;
			}
		}
		for (int j = 1; j >= i; j--) {
			node.data[j+1] = node.data[j];
			node.children[j+2] = node.children[j+1];
		}
		node.data[i] = x;
		node.children[i+1] = tobeAdd;
	}

	private int numOfData(Node node) {
		int i = 0;
		for (; i < 3; i++) {
			if (node.data[i] == StringSSet.EMPTY) {
				break;
			}
		}
		return i;
	}

	private String nodeToString(Node node) {
		String ret = "";
		for (int i = 0; i < 3; i++) {
			if (node.data[i] == StringSSet.EMPTY) {
				break;
			} else {
				if (i != 0) {
					ret += ",";
				}
				ret += node.data[i];
			}
		}
		return ret;
	}

	private Node lastChild(Node node) {
		for (int i = 3; i >= 0; i--) {
			if (node.children[i] != StringSSet.NIL) {
				return node.children[i];
			}
		}
		return StringSSet.NIL;
	}
}
