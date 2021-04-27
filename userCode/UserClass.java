package org.parser;

/**
 * <h1> UserClass </h1>
 *
 * This class is used to test the operation of the application, providing recursive methods to be parsed and converted into the corresponding iterative versions.
 */
public class UserClass {
    public int x;
    private double y;

    public int fattoriale(int dim) {
        if (dim == 0) {
            return 1;
        }
        return dim * fattoriale(dim - 1);
    }

    static void bubbleSort(int vec[], int dim) {
        if (dim == 1)
            return;

        for (int i = 0; i < dim - 1; i++) {
            if (vec[i] > vec[i + 1]) {
                int temp = vec[i];
                vec[i] = vec[i + 1];
                vec[i + 1] = temp;
            }
        }

        bubbleSort(vec, dim - 1);
    }

    static int minIndex(int a[], int i, int j) {
        if (i == j)
            return i;

        int k = minIndex(a, i + 1, j);

        return (a[i] < a[k]) ? i : k;
    }

    static void recurSelectionSort(int a[], int n, int index) {
        if (index == n)
            return;

        int k = minIndex(a, index, n - 1);

        if (k != index) {
            int temp = a[k];
            a[k] = a[index];
            a[index] = temp;
        }
        recurSelectionSort(a, n, index + 1);
    }

    static void insertionSortRecursive(int arr[], int n) {
        if (n <= 1)
            return;

        insertionSortRecursive(arr, n - 1);

        int last = arr[n - 1];
        int j = n - 2;

        while (j >= 0 && arr[j] > last) {
            arr[j + 1] = arr[j];
            j--;
        }
        arr[j + 1] = last;
    }

    public int partition(int arr[], int low, int high) {
        int pivot = arr[high];
        int i = (low - 1);
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;

                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }

        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1;
    }

    public void quickSort(int arr[], int l, int h) {
        if (l < h) {
            int pi = partition(arr, l, h);

            quickSort(arr, l, pi - 1);
            quickSort(arr, pi + 1, h);
        }
    }

    public void merge(int arr[], int l, int m, int r) {
        int n1 = m - l + 1;
        int n2 = r - m;

        int L[] = new int[n1];
        int R[] = new int[n2];

        for (int i = 0; i < n1; ++i)
            L[i] = arr[l + i];
        for (int j = 0; j < n2; ++j)
            R[j] = arr[m + 1 + j];

        int i = 0, j = 0;
        int k = l;

        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }

        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
    }

    public void mergeSort(int arr[], int l, int r) {
        if (l < r) {
            int m = (l + r) / 2;

            mergeSort(arr, l, m);
            mergeSort(arr, m + 1, r);
            merge(arr, l, m, r);
        }
    }
}