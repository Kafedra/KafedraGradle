
package com.jsptree.bean;

import java.util.ArrayList;
import java.util.List;

import ru.guap.treeview.GroupLoadItem;
import ru.guap.treeview.GroupStream;

/**
 * Узел древовидной структуры
 * @author Cr0s
 */
public class Node {
	/**
	 *  Идентификатор узла
	 */
	private String nodeId;
	
	/**
	 *  Название узла
	 */
	private String nodeName;
	
	/**
	 *  Статус узла (отмечен галочкой или нет)
	 */
	private int isSelected;
	
	/**
	 * Назначена ли нагрузка этого узла
	 */
	private boolean isAppointed;
	
	/**
	 * Этот узел содержит больше одного потомка? (для потоков/групповых узлов)
	 */
	private boolean isMultiNode;
	
	/**
	 * Идентификатор потока
	 */
	private int streamId;
	
	/**
	 * Идентификатор (код) дисциплины
	 */
	private int discId;
	
	/**
	 * Список потомков узла
	 */
	private ArrayList<Node> childrens = new ArrayList<>();
	
	/**
	 * Контейнер для исключения, если оно произошло
	 */
	private String errorDescription;
	
	/**
	 * Это - узел, непосредственно означающий нагрузку (лист дерева)
	 */
	private boolean isLoadNode;

	/**
	 * Группа, прикреплённая к узлу
	 */
	private GroupLoadItem item;
	
	/**
	 * Поток, прикреплённый к узлу
	 */
	private GroupStream stream;
	
	/**
	 * Конструктор узла
	 * @param id идентификатор узла
	 */
	public Node(String id) {
		this.nodeId = id;
	}
	
	/**
	 * Конструктор узла
	 * @param id идентификатор узла
	 * @param name имя узла
	 */
	public Node(String id, String name) {
		this.nodeId = id;
		this.nodeName = name;
	}	
	
	public boolean isMultiNode() {
		return isMultiNode;
	}

	public GroupLoadItem getItem() {
		return item;
	}

	public void setItem(GroupLoadItem item) {
		this.item = item;
	}

	public void setMultiNode(boolean isMultiNode) {
		this.isMultiNode = isMultiNode;
	}

	public int getStreamId() {
		return streamId;
	}

	public void setStreamId(int streamId) {
		this.streamId = streamId;
	}

	public int getDiscId() {
		return discId;
	}

	public void setDiscId(int discId) {
		this.discId = discId;
	}

	/**
	 * Получает идентификатор узла
	 * 
	 * Если этот узел для целой дисциплины/потока, то возвращает хеш от идентификатора дисциплины и потока
	 * @return идентификатор узла
	 */
	public String getNodeId() {
		if (!this.isMultiNode) {
			return nodeId;
		} else {
			return new Integer(this.discId + 23 * this.streamId).toString();
		}
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public ArrayList<Node> getChildren() {
		return childrens;
	}

	public void setChildren(ArrayList<Node> childrens) {
		this.childrens = childrens;
	}
	
	public void addChildNode(Node child) {
		this.childrens.add(child);
	}

	public int getIsSelected() {
		return isSelected;
	}

	public void setIsSelected(int isSelected) {
		this.isSelected = isSelected;
	}

	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
	
	public boolean isLoadNode() {
		return isLoadNode;
	}
	
	public void setLoadNode(boolean load) {
		this.isLoadNode = load;
	}
	
	public void setAppointed(boolean isAppointed) {
		this.isAppointed = isAppointed;
	}
	
	public boolean isAppointed() {
		return this.isAppointed;
	}

	public GroupStream getStream() {
		return stream;
	}

	public void setStream(GroupStream stream) {
		this.stream = stream;
	}
}


