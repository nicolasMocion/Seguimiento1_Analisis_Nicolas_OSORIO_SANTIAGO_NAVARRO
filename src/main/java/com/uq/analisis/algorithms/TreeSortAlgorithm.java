package com.uq.analisis.algorithms;

import com.uq.analisis.model.FinancialAsset;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TreeSortAlgorithm implements SortingAlgorithm {

    // Clase interna para los nodos del árbol
    class Node {
        FinancialAsset asset;
        Node left, right;

        public Node(FinancialAsset item) {
            asset = item;
            left = right = null;
        }
    }

    private Node root;
    private int currentIndex;

    @Override
    public void sort(List<FinancialAsset> data) {
        if (data == null || data.isEmpty()) return;

        root = null;
        currentIndex = 0;

        // 1. Insertar todos los elementos en el Árbol Binario
        for (FinancialAsset asset : data) {
            root = insertRec(root, asset);
        }

        // 2. Extraer los elementos en orden y sobrescribir la lista original
        storeInOrder(root, data);
    }

    @Override
    public String getName() {
        return "Tree Sort";
    }

    private Node insertRec(Node root, FinancialAsset asset) {
        if (root == null) {
            root = new Node(asset);
            return root;
        }
        // Usamos nuestro criterio de comparación
        if (compare(asset, root.asset) < 0) {
            root.left = insertRec(root.left, asset);
        } else {
            root.right = insertRec(root.right, asset);
        }
        return root;
    }

    private void storeInOrder(Node root, List<FinancialAsset> data) {
        if (root != null) {
            storeInOrder(root.left, data);
            data.set(currentIndex++, root.asset);
            storeInOrder(root.right, data);
        }
    }

    private int compare(FinancialAsset a, FinancialAsset b) {
        int dateComparison = a.getDate().compareTo(b.getDate());
        if (dateComparison != 0) {
            return dateComparison;
        }
        return a.getClose().compareTo(b.getClose());
    }
}