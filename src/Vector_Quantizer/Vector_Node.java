package Vector_Quantizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class Vector_Node {

    private int blockCol;
    private int blockRow;

    private Double[][] Avg_Block;
    private int [][] Q_1_Block;
    
    private static LinkedList<Vector_Node> Tree_Leaves = new LinkedList<Vector_Node>();
    private LinkedList<int[][]> List_Of_Blocks = new LinkedList<>();

    
    private int Q;
    private String  Str_Block;

    
    public String getStr_Block() {
        return Str_Block;
    }

    public void setStr_Block(String Str_Block) {
        this.Str_Block = Str_Block;
    }


    public int getQ() {
        return Q;
    }

    public void setQ(int Q) {
        this.Q = Q;
    }

    public int[][] getQ_1_Block() {
        return Q_1_Block;
    }

    public void setQ_1_Block(int[][] Q_1_Block) {
        this.Q_1_Block = Q_1_Block;
    }
 

    

    public int getBlockCol() {
        return blockCol;
    }

    public void setBlockCol(int blockCol) {
        this.blockCol = blockCol;
    }

    public int getBlockRow() {
        return blockRow;
    }

    public void setBlockRow(int blockRow) {
        this.blockRow = blockRow;
    }

   
//****************************************************************************    

    Vector_Node(Double avgArr[][]) {
        this.Avg_Block = avgArr;

    }

    public Vector_Node() {

    }

    public Double[][] getAvgBlock() {
        return this.Avg_Block;
    }

    public void setAvgBlock(Double avg1[][]) {
        this.Avg_Block = avg1;
    }

    public Double[][] Get_Settled_Average(Vector_Node node) {
        int row=node.getAvgBlock().length;
        int col=node.getAvgBlock()[0].length;
        int listsize=node.getList_Of_Blocks().size();
        
        Double avgBlock[][] = new Double[row][col];

        for (int j = 0; j < row; j++) {
            for (int k = 0; k < col; k++) {
                avgBlock[j][k] = 0.0;
            }
        }

        for (int i = 0; i < listsize ; i++) {
            for (int j = 0; j < row; j++) {

                for (int k = 0; k < col; k++) {

                    avgBlock[j][k] += node.getList_Of_Blocks().get(i)[j][k];
                }

            }
        }
        
        for (int i = 0; i < row; i++) {
            
            for (int j = 0; j < col; j++) {
                if (node.getList_Of_Blocks().size() != 0) 
                {
                    avgBlock[i][j] = avgBlock[i][j] / node.getList_Of_Blocks().size();
                }
            }
        }
        
        return avgBlock;
    }

    public LinkedList<int[][]> getList_Of_Blocks() {
        return List_Of_Blocks;
    }

    public void setList_Of_Blocks(LinkedList<int[][]> list1) {

        for (int i = 0; i < list1.size(); i++) {
            this.List_Of_Blocks.add(list1.get(i));
        }
    }

    public Double[][] GetAverageHistory(Double block[][], int number) {
        Double temp[][] = new Double[block.length][block[0].length];
        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[0].length; j++) {
                temp[i][j] = block[i][j];
            }
        }
        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[0].length; j++) {

                temp[i][j] += number;
            }
        }

        return temp;
    }

    public void Tree_Construction( int numberOfLevels,LinkedList<int[][]>listOfBlocks) {
        int c = 0;
        
            LinkedList<Double> Error = new LinkedList<>();

            Vector_Node root =new Vector_Node();
            
            int blockrow=0;
            int blockcol =0;
            
            
            if(listOfBlocks.size()!=0)
            {
             blockrow = listOfBlocks.get(0).length;
             blockcol = listOfBlocks.get(0)[0].length;
            }
          
            int noBlocks = listOfBlocks.size();
            //========================Getting Avg Block=====================================
             Double avgBlock[][] =new Double[blockrow][blockcol];


            for (int j = 0; j < blockrow; j++) {
                for (int k = 0; k < blockcol; k++) {
                    avgBlock[j][k] = 0.0;
                }
            }
            
        

        for (int i = 0; i < listOfBlocks.size(); i++) {
            for (int j = 0; j < blockrow; j++) {
                for (int k = 0; k < blockcol; k++) {
                    avgBlock[j][k] += listOfBlocks.get(i)[j][k];
                }
            }
            
        }
        
        for (int i = 0; i < blockrow; i++) {
            for (int j = 0; j < blockcol; j++) {
                avgBlock[i][j] /= noBlocks;
            }
        }
//=================================================================================
 
           
            
            root.setAvgBlock(avgBlock);
            
            root.setBlockRow(blockrow);
            root.setBlockCol(blockcol);
            
            root.setList_Of_Blocks(listOfBlocks);
            Tree_Leaves.add(root);
            
        LinkedList<Vector_Node>TemporaryContainer = new LinkedList<>();
        
        if (Tree_Leaves.size()!=numberOfLevels){
            
        while (Tree_Leaves.size() != numberOfLevels) {
               
            System.out.println("C = "+c);
            c++;
            for (int i = 0; i < Tree_Leaves.size(); i++) {
                Vector_Node parent = Tree_Leaves.get(i);
                
                Double historyAVG[][] = parent.Get_Settled_Average(parent);
                
                Vector_Node leftNode = new Vector_Node(GetAverageHistory(historyAVG, -1));                
                leftNode.setBlockRow(blockrow);
                leftNode.setBlockCol(blockcol);
                
                Vector_Node rightNode = new Vector_Node(GetAverageHistory(historyAVG, 1));
                rightNode.setBlockRow(blockrow);
                rightNode.setBlockCol(blockcol);
                
                TemporaryContainer.add(leftNode);
                TemporaryContainer.add(rightNode);
            }
            Tree_Leaves.clear();
            //***************************comparison*****************************
                for (int j = 0; j < listOfBlocks.size(); j++) {
                    
                    for (int k = 0; k < TemporaryContainer.size(); k++) {

                        Double diff = CompareDiffrence(listOfBlocks.get(j), TemporaryContainer.get(k).getAvgBlock());
                        
                        Error.add(diff);
                    }
                    int index = Error.indexOf(Collections.min(Error));
                    
                    TemporaryContainer.get(index).getList_Of_Blocks().add(listOfBlocks.get(j));
                    
                    Error.clear();
                }
                
                for (int i = 0; i < TemporaryContainer.size(); i++) {
                Tree_Leaves.add(TemporaryContainer.get(i));
                }
                
                TemporaryContainer.clear();
            

        }
        }

    }

    
    public Double CompareDiffrence(int block[][], Double avg[][]) {
        Double distance_1 = 0.0;
        Double blockDiff_1[][] = new Double[block.length][block[0].length];

        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[0].length; j++) {
                blockDiff_1[i][j] = Math.abs(block[i][j] - avg[i][j]);

            }
        }
        for (int i = 0; i < block.length; i++) {
            for (int j = 0; j < block[0].length; j++) {
                distance_1 += blockDiff_1[i][j];

            }
        }
        return distance_1;
    }

 
    public LinkedList<Vector_Node> Accurracy_Enhancer( LinkedList<int[][]> arrayOfBlocks ) {

        LinkedList<Double> Error = new LinkedList<>();
        int maxNumberOfItterations = 100;
        int i = 0;
        for (i = 0; i < maxNumberOfItterations; i++) {
            boolean error = false;
            for (int j = 0; j < Tree_Leaves.size(); j++) {
               
                if (Arrays.deepEquals(Tree_Leaves.get(j).getAvgBlock(), Tree_Leaves.get(j).Get_Settled_Average(Tree_Leaves.get(j))) == false) {
                    error = true;
                    break;

                } else {
                    error = false;
                }
            }
            if (error == false) 
            {
                break;
            } else if (error == true) 
            
            {
                
                for (int j = 0; j < Tree_Leaves.size(); j++) 
                {
                    
                  Double x[][] = Tree_Leaves.get(j).Get_Settled_Average(Tree_Leaves.get(j));
                    Tree_Leaves.get(j).setAvgBlock(x);                    
                    Tree_Leaves.get(j).getList_Of_Blocks().clear();
                }
                
//***************************comparison***************************
                for (int j = 0; j < arrayOfBlocks.size(); j++) {
                    for (int k = 0; k < Tree_Leaves.size(); k++) {

                        Double diff = CompareDiffrence(arrayOfBlocks.get(j), Tree_Leaves.get(k).getAvgBlock());
                        Error.add(diff);
                    }

                    int index = Error.indexOf(Collections.min(Error));

                    Tree_Leaves.get(index).getList_Of_Blocks().add(arrayOfBlocks.get(j));
                    Error.clear();
                }

            }
        }
        return Tree_Leaves;
    }


    public void Print() {
        int row=0;
        int col=0;
        
        row=Tree_Leaves.get(0).getAvgBlock().length;
        col=Tree_Leaves.get(0).getAvgBlock()[0].length;
        
        
        System.out.println("____FINALS_________");
        for (int i = 0; i < Tree_Leaves.size(); i++) {
            
            System.out.println("for each node "+(i+1)+" have those matrices ");
            for (int j = 0; j < Tree_Leaves.get(i).getList_Of_Blocks().size(); j++)
            {
                System.out.println("matrix no "+(j+1));
                for (int k = 0; k < row; k++) {
                    for (int l = 0; l < col; l++) {
                        System.out.print(Tree_Leaves.get(i).getList_Of_Blocks().get(j)[k][l]+"   ");
                    }
                    System.out.println();
                }
                System.out.println("=======");
            }
        }

    }

}
