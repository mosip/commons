package io.mosip.kernel.core.masterdata.util.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * Node in an unbalanced hierarchy tree of master-data records.
 * <p>
 * Contract: {@code id} uniquely identifies this node; {@code parentId} is
 * null for roots. Children are added via {@link #addChild(Node)}. Used with
 * {@link io.mosip.kernel.core.masterdata.util.spi.UBtree}. Does not perform
 * I/O.
 * </p>
 *
 * @param <T> payload type stored on the node
 * @author Abhishek Kumar
 * @since 1.0.0
 */
@Getter
public class Node<T> {
	/**
	 * Domain value held by this node; may be null.
	 */
	private T value;
	/**
	 * Child nodes; null until the first child is added.
	 */
	private List<Node<T>> childs;
	/**
	 * Parent node identifier; null for a root.
	 */
	private String parentId;
	/**
	 * Unique identifier of this node; should be non-blank.
	 */
	private String id;
	/**
	 * Parent node reference; null for a root until linked.
	 */
	private Node<T> parent;

	/**
	 * Constructs a node with identity, value, and parent id.
	 *
	 * @param id       never-null node identifier
	 * @param value    domain payload; may be null
	 * @param parentId parent identifier; null for a root
	 */
	public Node(String id, T value, String parentId) {
		this.id = id;
		this.parentId = parentId;
		this.value = value;
	}

	/**
	 * Sets this node's unique identifier.
	 *
	 * @param id never-null identifier
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Appends a child node.
	 *
	 * @param child never-null child to add
	 * @return {@code true} if the child was added
	 */
	public boolean addChild(Node<T> child) {
		if (childs == null) {
			childs = new ArrayList<>();
		}
		return childs.add(child);
	}

	/**
	 * Appends multiple child nodes.
	 *
	 * @param list never-null list of children; may be empty
	 * @return {@code true} if the list changed
	 */
	public boolean addChilds(List<Node<T>> list) {
		if (childs == null) {
			childs = new ArrayList<>();
		}
		return childs.addAll(list);
	}

	/**
	 * Sets the domain payload.
	 *
	 * @param value payload; may be null
	 */
	public void setValue(T value) {
		this.value = value;
	}

	/**
	 * Sets the parent identifier.
	 *
	 * @param parentId parent id; null for a root
	 */
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	/**
	 * Sets the parent node reference.
	 *
	 * @param parent parent node; null for a root
	 */
	public void setParent(Node<T> parent) {
		this.parent = parent;
	}
}
