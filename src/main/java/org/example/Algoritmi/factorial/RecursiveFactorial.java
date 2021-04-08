package org.example.Algoritmi.factorial;

import java.util.ArrayList;
import java.util.List;

public class RecursiveFactorial {

        public int fattoriale (int n)
        {
            if ( n == 0 ) return 1;
            return n * fattoriale(n-1);
        }
}
