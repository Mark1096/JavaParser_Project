package org.example.Algoritmi.bubblesort;

public class IterativeBubbleSort {
    public void bubbleSort(int arr[], int n)
    {
        for (int i = 0; i < n-1; i++) {
            for (int j = 0; j < arr[j + 1]; j++){
                int tmp = arr[j];
                arr[j] = arr[j+1];
                arr[j+1] = tmp;
            }
        }
    }
}
