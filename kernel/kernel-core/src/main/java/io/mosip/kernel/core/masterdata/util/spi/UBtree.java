package io.mosip.kernel.core.masterdata.util.spi;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import io.mosip.kernel.core.masterdata.util.model.Node;

/**
 * Unbalanced tree helpers for MOSIP master-data location / hierarchy graphs.
 * <p>
 * Contract: default methods operate in memory (no HTTP/DB). {@code convertToNode}
 * must map a domain object to a {@link Node} with id and parentId. Null lists
 * and nodes yield empty results rather than throwing, except
 * {@link #getParentHierarchy(Node)} which requires a non-null node.
 * </p>
 *
 * @param <T> domain type stored on each {@link Node}
 * @author Abhishek Kumar
 * @since 1.0.0
 */
public interface UBtree<T> {

	/**
	 * Builds parent/child links from a flat list of domain objects.
	 *
	 * @param list domain objects; null yields an empty list
	 * @return never-null list of linked nodes
	 */
	public default List<Node<T>> createTree(List<T> list) {
		if (list == null) {
			return Collections.emptyList();
		}
		Map<String, Node<T>> mapTmp = new HashMap<>();

		// convert into node
		List<Node<T>> nodes = list.stream().map(this::convertToNode).collect(Collectors.toList());
		// Save all nodes to a map
		for (Node<T> current : nodes) {
			mapTmp.put(current.getId(), current);
		}
		// loop and assign parent/child relationships
		for (Node<T> current : nodes) {
			String parentId = current.getParentId();
			if (parentId != null) {
				Node<T> parent = mapTmp.get(parentId);
				if (parent != null) {
					current.setParent(parent);
					parent.addChild(current);
					mapTmp.put(parentId, parent);
					mapTmp.put(current.getId(), current);
				}
			}
		}
		return nodes;
	}

	/**
	 * Returns leaf descendants of {@code node} (nodes with no children).
	 *
	 * @param node subtree root; null yields an empty list
	 * @return never-null leaf list; empty if {@code node} is null or has no leaves
	 */
	public default List<Node<T>> findLeafs(Node<T> node) {
		if (node == null) {
			return Collections.emptyList();
		}
		List<Node<T>> flatList = new ArrayList<>();
		Deque<Node<T>> q = new ArrayDeque<>();
		q.addLast(node);

		while (!q.isEmpty()) {
			Node<T> n = q.removeLast();
			List<Node<T>> children = n.getChilds();
			if (children != null) {
				for (Node<T> child : children) {
					q.addLast(child);
				}
			} else {
				flatList.add(n);
			}
		}

		return flatList;
	}

	/**
	 * Returns the domain values of leaf descendants of {@code node}.
	 *
	 * @param node subtree root; null yields an empty list
	 * @return never-null list of leaf values
	 */
	public default List<T> findLeafsValue(Node<T> node) {
		if (node == null) {
			return Collections.emptyList();
		}
		List<Node<T>> nodes = findLeafs(node);
		return nodes.stream().map(Node::getValue).collect(Collectors.toList());
	}

	/**
	 * Walks parent links from {@code node} until a node with no parent is found.
	 *
	 * @param node starting node; null is returned as-is
	 * @return the root node, or {@code node} itself if it is already a root or null
	 */
	public default Node<T> findRootNode(Node<T> node) {
		if (node == null) {
			return node;
		}
		// get the root
		Node<T> root = node;
		boolean flag = true;
		while (flag) {
			if (root.getParent() != null) {
				root = root.getParent();
			} else {
				flag = false;
			}
		}
		return root;
	}

	/**
	 * Returns the domain value of the root ancestor of {@code node}.
	 *
	 * @param node starting node; null yields null
	 * @return root payload; may be null
	 */
	public default T findRootNodeValue(Node<T> node) {
		if (node == null) {
			return null;
		}
		return findRootNode(node).getValue();
	}

	/**
	 * Returns {@code node} plus all descendant domain values (breadth-first).
	 *
	 * @param node subtree root; null yields an empty list
	 * @return never-null list of values from {@code node} downward
	 */
	public default List<T> getChildHierarchy(Node<T> node) {
		if (node == null) {
			return Collections.emptyList();
		}
		List<T> flatList = new ArrayList<>();
		Deque<Node<T>> q = new ArrayDeque<>();
		q.addLast(node);
		flatList.add(node.getValue());
		while (!q.isEmpty()) {
			Node<T> n = q.removeLast();
			List<Node<T>> children = n.getChilds();
			if (children != null) {
				for (Node<T> child : children) {
					q.addLast(child);
					flatList.add(child.getValue());
				}
			}
		}

		return flatList;

	}

	/**
	 * Returns {@code node} plus all ancestor domain values up to the root.
	 *
	 * @param node never-null starting node
	 * @return never-null list from {@code node} to root
	 * @throws NullPointerException if {@code node} is null
	 */
	public default List<T> getParentHierarchy(Node<T> node) {
		Objects.requireNonNull(node);
		// get the root
		List<T> data = new ArrayList<>();
		Node<T> root = node;
		data.add(node.getValue());
		boolean flag = true;
		while (flag) {
			if (root.getParent() != null) {
				root = root.getParent();
				data.add(root.getValue());
			} else {
				flag = false;
			}
		}
		return data;
	}

	/**
	 * Depth-first search for a node with {@code id} under {@code root}.
	 *
	 * @param root subtree to search; null yields null
	 * @param id   never-null node identifier to match
	 * @return matching node, or null if not found
	 */
	public default Node<T> searchNode(Node<T> root, String id) {
		if (root == null) {
			return null;
		}
		Deque<Node<T>> q = new ArrayDeque<>();
		q.addLast(root);
		while (!q.isEmpty()) {
			Node<T> n = q.removeLast();
			if (n.getId().equals(id)) {
				return n;
			} else {
				List<Node<T>> children = n.getChilds();
				if (children != null) {
					for (Node<T> child : children) {
						q.addLast(child);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Finds the first node in {@code list} whose id equals {@code id}.
	 *
	 * @param list never-null node list; must not contain null elements
	 * @param id   never-null identifier to match
	 * @return matching node, or null if none matches
	 */
	public default Node<T> findNode(List<Node<T>> list, String id) {
		Optional<Node<T>> node = list.stream().filter(i -> i.getId().equals(id)).findAny();
		return node.isPresent() ? node.get() : null;
	}

	/**
	 * Converts a domain object into a tree {@link Node}.
	 *
	 * @param node never-null domain object
	 * @return never-null node with id and parentId populated
	 */
	public Node<T> convertToNode(T node);
}
