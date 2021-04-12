package org.example.Algoritmi.factorial;

public class IterativeFactorial {
    public int fattoriale(int n)
    {
        int f = 1;
        while (n > 0)	f *= n-- ;
        return f;
    }
}
