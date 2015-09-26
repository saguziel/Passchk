import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class PasswordClassifier
{
    public static void main(String[] args)
        throws IOException
    {
        Charset localCharset = Charset.forName("UTF-8");
        Path dictPath = FileSystems.getDefault().getPath("", args[0]);
        List<String> dictWords = Files.readAllLines(dictPath, localCharset);

        Trie dictTrie = new Trie(dictWords);

        HashSet<String> words = new HashSet<>();
        for (String password: dictWords) {
            words.add(password);
        }

        Path inFile = FileSystems.getDefault().getPath("", args[1]);
        Path outFile = FileSystems.getDefault().getPath("", args[2]);

        List<String> inPasswords = Files.readAllLines(inFile, localCharset);
        StringBuffer outputBuffer = new StringBuffer();
        for (String str : inPasswords) {
            outputBuffer.append(str);
            outputBuffer.append(", ");
            if (classifyPassword(str, dictTrie, words) > 28.0) {
                outputBuffer.append("strong\n");
            } else {
                outputBuffer.append("weak\n");
            }
        }
        Files.write(outFile, outputBuffer.toString().getBytes(localCharset), 
                StandardOpenOption.CREATE);
    }

    public static double classifyPassword(String password, PasswordClassifier.Trie paramTrie, HashSet<String> paramHashSet)
    {
        String str = password.toLowerCase()
            .replace('4', 'a')
            .replace('@', 'a')
            .replace('8', 'b')
            .replace('9', 'g')
            .replace('6', 'g')
            .replace('3', 'e')
            .replace('2', 'z')
            .replace('5', 's')
            .replace('7', 't')
            .replace('0', 'o')
            .replace('!', 'i')
            .replace('$', 's');
        ArrayList localArrayList1 = splitWords(str, paramTrie, paramHashSet);
        ArrayList localArrayList2 = splitWords(str, paramHashSet);
        int i = 0;
        int j = 0;
        for (Iterator localIterator = localArrayList1.iterator(); localIterator.hasNext();)
        {
            PasswordClassifier.Range localRange = (PasswordClassifier.Range)localIterator.next();
            i += localRange.high - localRange.low;
        }
        PasswordClassifier.Range localRange;
        for (Iterator localIterator = localArrayList2.iterator(); localIterator.hasNext();)
        {
            localRange = (PasswordClassifier.Range)localIterator.next();
            j += localRange.high - localRange.low;
        }
        double d1 = i / password.length();
        double d2 = j / password.length();

        double d3 = 0.0D;

        d3 += 2.0D * (1.0D - d1);
        d3 += 6.0D * (1.0D - d2);
        d3 += lengthEntropy(password.length());
        if (specialCharEntropy(password) > 0.0D) {
            d3 += 3.0D;
        }
        d3 += 4.0D * specialCharEntropy(password);
        if (caseEntropy(password) > 0.0D) {
            d3 += 2.0D;
        }
        d3 += 4.0D * caseEntropy(password);
        d3 += numberPenalty(password);
        d3 += numberPenalty(new StringBuffer(password).reverse().toString());

        return d3;
    }

    public static double lengthEntropy(int paramInt)
    {
        double d = 0.0D;
        if (paramInt == 0) {
            return 0.0D;
        }
        d += 4.0D;
        paramInt--;
        if (paramInt == 0) {
            return d;
        }
        d += 2.0D * Math.min(7, paramInt);
        paramInt -= Math.min(7, paramInt);
        if (paramInt == 0) {
            return d;
        }
        d += 1.5D * Math.min(12, paramInt);
        paramInt -= Math.min(12, paramInt);
        if (paramInt == 0) {
            return d;
        }
        d += paramInt;
        return d;
    }

    public static double specialCharEntropy(String paramString)
    {
        int i = 0;
        for (int j = 0; j < paramString.length(); j++) {
            if ((!Character.isDigit(paramString.charAt(j))) && (!Character.isLetter(paramString.charAt(j)))) {
                i++;
            }
        }
        return i / paramString.length();
    }

    public static double numberEntropy(String paramString)
    {
        int i = 0;
        for (int j = 0; j < paramString.length(); j++) {
            if (Character.isDigit(paramString.charAt(j))) {
                i++;
            }
        }
        return i / paramString.length();
    }

    public static double caseEntropy(String paramString)
    {
        int i = 0;
        int j = 0;
        for (int k = 0; k < paramString.length(); k++) {
            if (Character.isLetter(paramString.charAt(k))) {
                if (Character.isLowerCase(paramString.charAt(k))) {
                    j++;
                } else {
                    i++;
                }
            }
        }
        if (i + j == 0) {
            return 0.0D;
        }
        return 2.0D * (0.5D - Math.abs(i / (i + j) - 0.5D));
    }

    public static double numberPenalty(String paramString)
    {
        double d = 0.0D;
        paramString = paramString.toLowerCase();
        for (int i = 0; i < paramString.length(); i++)
        {
            if ((paramString.startsWith("12", i)) || 
                    (paramString.startsWith("23", i)) || 
                    (paramString.startsWith("34", i)) || 
                    (paramString.startsWith("45", i)) || 
                    (paramString.startsWith("56", i)) || 
                    (paramString.startsWith("67", i)) || 
                    (paramString.startsWith("78", i)) || 
                    (paramString.startsWith("89", i)) || 
                    (paramString.startsWith("98", i))) {
                d -= 1.0D;
                    }
            if ((paramString.startsWith("11", i)) || 
                    (paramString.startsWith("22", i)) || 
                    (paramString.startsWith("33", i)) || 
                    (paramString.startsWith("44", i)) || 
                    (paramString.startsWith("55", i)) || 
                    (paramString.startsWith("66", i)) || 
                    (paramString.startsWith("77", i)) || 
                    (paramString.startsWith("88", i)) || 
                    (paramString.startsWith("99", i)) || 
                    (paramString.startsWith("00", i))) {
                d -= 0.5D;
                    }
        }
        return d;
    }

    public static ArrayList<PasswordClassifier.Range> splitWords(String paramString, HashSet<String> paramHashSet)
    {
        int[] arrayOfInt1 = new int[paramString.length() + 1];
        PasswordClassifier.Range[] arrayOfRange = new PasswordClassifier.Range[paramString.length() + 1];
        int[] arrayOfInt2 = new int[paramString.length() + 1];

        arrayOfInt1[0] = 0;
        arrayOfRange[0] = null;
        arrayOfInt2[0] = 0;
        for (int k = 1; k < paramString.length() + 1; k++)
        {
            int i = 0;
            int j = 0;
            PasswordClassifier.Range localRange = null;
            for (int m = 0; m <= k; m++) {
                if ((paramHashSet.contains(paramString.substring(m, k))) && (k - m + arrayOfInt2[m] > i))
                {
                    i = arrayOfInt2[m] + k - m;
                    j = m;
                    localRange = new PasswordClassifier.Range(m, k);
                }
            }
            for (int m = 0; m < k; m++) {
                if (arrayOfInt2[m] > i)
                {
                    i = arrayOfInt2[m];
                    j = m;
                    localRange = null;
                }
            }
            arrayOfInt1[k] = j;
            arrayOfRange[k] = localRange;
            arrayOfInt2[k] = i;
        }
        ArrayList localArrayList = new ArrayList();
        int m = paramString.length();
        while (m != 0)
        {
            if (arrayOfRange[m] != null) {
                localArrayList.add(arrayOfRange[m]);
            }
            m = arrayOfInt1[m];
        }
        Collections.reverse(localArrayList);
        return localArrayList;
    }

    public static ArrayList<PasswordClassifier.Range> splitWords(String paramString, PasswordClassifier.Trie paramTrie, HashSet<String> paramHashSet)
    {
        int[] arrayOfInt1 = new int[paramString.length() + 1];
        PasswordClassifier.Range[] arrayOfRange = new PasswordClassifier.Range[paramString.length() + 1];
        int[] arrayOfInt2 = new int[paramString.length() + 1];

        arrayOfInt1[0] = 0;
        arrayOfRange[0] = null;
        arrayOfInt2[0] = 0;
        for (int m = 1; m < paramString.length() + 1; m++)
        {
            int i = 0;
            int j = 0;
            PasswordClassifier.Range localRange = null;
            for (int n = 0; n <= m; n++)
            {
                int k = paramTrie.traverse(paramString.substring(n, m));
                if ((k + arrayOfInt2[n] > i) && ((k > 2) || (paramHashSet.contains(paramString.substring(n, m)))))
                {
                    i = k + arrayOfInt2[n];
                    j = n;
                    localRange = new PasswordClassifier.Range(n, n + k);
                }
            }
            arrayOfInt1[m] = j;
            arrayOfRange[m] = localRange;
            arrayOfInt2[m] = i;
        }
        ArrayList localArrayList = new ArrayList();
        int n = paramString.length();
        while (arrayOfRange[n] != null)
        {
            localArrayList.add(arrayOfRange[n]);
            n = arrayOfInt1[n];
        }
        Collections.reverse(localArrayList);
        return localArrayList;
    }

    public static class Range
    {
        int low;
        int high;

        public Range(int paramInt1, int paramInt2)
        {
            this.low = paramInt1;
            this.high = paramInt2;
        }

        public String toString()
        {
            return String.format("(%d, %d)", new Object[] { Integer.valueOf(this.low), Integer.valueOf(this.high) });
        }
    }

    public static class Trie
    {
        final PasswordClassifier.TrieNode root;

        public Trie(List<String> paramList)
        {
            this.root = new PasswordClassifier.TrieNode(0);
            for (String str : paramList) {
                insertWord(str);
            }
        }

        public Trie(List<String> paramList, boolean paramBoolean)
        {
            this.root = new PasswordClassifier.TrieNode(0);
            for (String str : paramList) {
                insertWord2(str);
            }
        }

        public void insertWord2(String paramString)
        {
            PasswordClassifier.TrieNode localTrieNode = this.root;
            for (int i = 0; i < paramString.length(); i++) {
                localTrieNode = localTrieNode.add(paramString.charAt(i));
            }
        }

        public void insertWord(String paramString)
        {
            PasswordClassifier.TrieNode localTrieNode = this.root;
            for (int i = 0; i < paramString.length(); i++)
            {
                for (int j = i; j < paramString.length(); j++) {
                    localTrieNode = localTrieNode.add(paramString.charAt(j));
                }
                localTrieNode = this.root;
            }
        }

        public int traverse(String paramString)
        {
            PasswordClassifier.TrieNode localTrieNode = this.root;
            int i = 0;
            int j = 0;
            while ((localTrieNode != null) && (i < paramString.length()))
            {
                localTrieNode = localTrieNode.get(paramString.charAt(i));
                if (localTrieNode != null) {
                    j++;
                }
                i++;
            }
            return j;
        }
    }

    public static class TrieNode
    {
        public HashMap<Character, TrieNode> map;
        public int depth;

        public TrieNode(int paramInt)
        {
            this.depth = paramInt;
            this.map = new HashMap();
        }

        public TrieNode add(char paramChar)
        {
            if (this.map.containsKey(Character.valueOf(paramChar))) {
                return (TrieNode)this.map.get(Character.valueOf(paramChar));
            }
            TrieNode localTrieNode = new TrieNode(this.depth + 1);
            this.map.put(Character.valueOf(paramChar), localTrieNode);
            return localTrieNode;
        }

        public TrieNode get(char paramChar)
        {
            if (this.map.containsKey(Character.valueOf(paramChar))) {
                return (TrieNode)this.map.get(Character.valueOf(paramChar));
            }
            return null;
        }
    }

