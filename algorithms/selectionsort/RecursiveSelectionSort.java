public class RecursiveSelectionSort {

    static void recurSelectionSort(int a[], int n, int index) {
        // Return when starting and size are same
        if (index == n)
            return;

        // calling minimum index function for minimum index
        int k = minIndex(a, index, n - 1);

        // Swapping when index nd minimum index are not same
        if (k != index) {
            // swap
            int temp = a[k];
            a[k] = a[index];
            a[index] = temp;
        }
        // Recursively calling selection sort function
        recurSelectionSort(a, n, index + 1);
    }

    static int minIndex(int a[], int i, int j) {
        if (i == j)
            return i;

        int k = minIndex(a, i + 1, j);

        return (a[i] < a[k]) ? i : k;
    }
}
