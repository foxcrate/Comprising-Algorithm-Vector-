package Vector_Quantizer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class VectorAlgo_Controller {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        import java.util.Queue;
//
//        Queue<Integer> q = new LinkedList<>();

        VectorQuantizer_Scene obj = new VectorQuantizer_Scene();
        obj.setDefaultCloseOperation(obj.EXIT_ON_CLOSE);
        obj.setLocationRelativeTo(null);
        obj.setVisible(true);

    }

    //C:\\Users\\ENG\\Desktop\\Quantizer\\o.jpg
    
    
    //C:\\Users\\ENG\\Desktop\\Quantizer\\cd.txt
    
    //C:\\Users\\ENG\\Desktop\\Quantizer\\o1.jpg

    
    public static int[][] readImage(String path) {

        BufferedImage img;
        try {
            img = ImageIO.read(new File(path));

            int hieght = img.getHeight();
            int width = img.getWidth();

            int[][] imagePixels = new int[hieght][width];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < hieght; y++) {

                    int pixel = img.getRGB(x, y);

                    int red = (pixel & 0x00ff0000) >> 16;
                    int grean = (pixel & 0x0000ff00) >> 8;
                    int blue = pixel & 0x000000ff;
                    int alpha = (pixel & 0xff000000) >> 24;
                    imagePixels[y][x] = red;
                }
            }

            return imagePixels;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return null;
        }

    }

    public static void writeImage(int[][] imagePixels, String outPath) {

      //  BufferedImage image = new BufferedImage(imagePixels.length, imagePixels[0].length, BufferedImage.TYPE_INT_RGB);
       BufferedImage image = new BufferedImage(imagePixels[0].length, imagePixels.length, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < imagePixels.length; y++) {
            for (int x = 0; x < imagePixels[y].length; x++) {
                int value = -1 << 24;
                value = 0xff000000 | (imagePixels[y][x] << 16) | (imagePixels[y][x] << 8) | (imagePixels[y][x]);
                image.setRGB(x, y, value);

            }
        }

        File ImageFile = new File(outPath);
        try {
            ImageIO.write(image, "jpg", ImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static int log(int x, int base) {
        return (int) (Math.log(x) / Math.log(base));
    }
    
    
    
    public  static void compress( String quant ,String source, String destination , String br, String bc)
    {
 
      int codebooksize = 0;

        if (quant.substring(0, 1).charAt(0) == 'b') 
        {
            int bits = Integer.parseInt(quant.substring(1, quant.length()));
            codebooksize = (int) Math.pow(2, bits);
        } 

        else if (quant.substring(0, 1).charAt(0) == 'l')
        {
            codebooksize = Integer.parseInt(quant.substring(1, quant.length()));
        }
        

       int blockrow=0;
       blockrow=Integer.parseInt(br);
       int blockcol=0;
       blockcol=Integer.parseInt(bc);


       
             int paddedimage[][]= padding(source,blockrow,blockcol);

            LinkedList<int [][] > blocks=Crop_Image(paddedimage, blockrow, blockcol);

            Vector_Node root = new Vector_Node();
            root.Tree_Construction(codebooksize,blocks);

            LinkedList<Vector_Node> Stable_Nodes = root.Accurracy_Enhancer(blocks);

                
        
//**************************COMPRESSION ***********************************
            String code = "";
   
        for (int i = 0; i < blocks.size(); i++) {
            for (int j = 0; j < Stable_Nodes.size(); j++) {
                for (int k = 0; k < Stable_Nodes.get(j).getList_Of_Blocks().size(); k++) {
                    if(Arrays.deepEquals(blocks.get(i) , Stable_Nodes.get(j).getList_Of_Blocks().get(k))){
                        code+=j+",";
                        break;
                    }
                }
            }
        }
        
        
//*****************************making avg integer************************************
LinkedList<int[][]> avgBlocks = new LinkedList<>();

            for (int i = 0; i < Stable_Nodes.size(); i++) {

                int int_Block[][] = new int[blockrow][blockcol];
                
                for (int j = 0; j < blockrow; j++) {
                    for (int k = 0; k < blockcol; k++) {
                        int_Block[j][k] = Stable_Nodes.get(i).getAvgBlock()[j][k].intValue();
                    }
                }
                avgBlocks.add(int_Block);
            }

//**********************************************************        
int imagerow=paddedimage.length;
int imagecol=paddedimage[0].length;
           
        PrintWriter pw = null;
        try 
        {
            pw = new PrintWriter(destination);
        } 
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

// writing to file         
      pw.write(code+"\n");
      pw.write(Integer.toString(imagerow)+"#"+Integer.toString(imagecol)+"\n");
      
      String AvgMatrix="";
      int Q=0;
       for (int i = 0; i < avgBlocks.size(); i++) {
             Q=i;
             int[][] avgblock = avgBlocks.get(i);
             AvgMatrix=Arrays.deepToString(avgblock);
             pw.write(Integer.toString(Q)+"*"+AvgMatrix+"\n");
   
        }
             

        pw.close();
        JOptionPane.showMessageDialog(null,"File Compressed Successfully!");
    }


    public static void decompress(String source, String destination)
    {
      
        String Compressed_String = null;
        
        try
        {
            Compressed_String = new Scanner(new File(source)).useDelimiter("\\Z").next();
        }
        catch (FileNotFoundException e) 
        {
            e.printStackTrace();
        }
        
        
        String[]Data_File= Compressed_String.split("\n");
                
        String code=Data_File[0];
        String codearr [] = code.split(",");
        
        String Image_Dimensions=Data_File[1];
        int Imagerow=Integer.parseInt(Data_File[1].substring(0,Data_File[1].indexOf('#'))) ;
        int imagecol=Integer.parseInt(Data_File[1].substring(Data_File[1].indexOf('#')+1,Data_File[1].length()));
        
        int Q=0;
        String AvgMatrix="";
        

        LinkedList<Vector_Node> Quantizer=new LinkedList<>();
        LinkedList<Vector_Node> Quantizer_Str=new LinkedList<>();
        
        for (int i = 2; i < Data_File.length; i++) 
        {
            
            Q=Integer.parseInt(Data_File[i].substring(0,Data_File[i].indexOf('*')));
            AvgMatrix=Data_File[i].substring(Data_File[i].indexOf('*')+1,Data_File[i].length());
            
            Vector_Node vector = new Vector_Node();
            vector.setQ(Q);
            vector.setStr_Block(AvgMatrix);
            Quantizer_Str.add(vector);
            
        }
        
        for (int i = 0; i < Quantizer_Str.size(); i++) 
        {
        // Split on this delimiter

        String[] rows = Quantizer_Str.get(i).getStr_Block().split("], \\[");
        
        for (int k = 0; k < rows.length; k++) 
        {
            // Remove any beginning and ending braces and any white spaces
            rows[k] = rows[k].replace("[[", "").replace("]]", "").replaceAll(" ", "");
        }

// Get the number of columns in a row
        int numberOfColumns = rows[0].split(",").length;

// Setup your matrix
        String[][] matrix = new String[rows.length][numberOfColumns];

// Populate your matrix
        for (int j = 0; j < rows.length; j++) 
        {
            matrix[j] = rows[j].split(",");
        }
                
//********** DONE READING THE MATRIX *********************************************

//************ changing matrix to integer ******************************
       int[][] Decompressed_Matrix = new int[matrix.length][matrix[0].length];
              
        for (int r = 0; r < Decompressed_Matrix.length; r++) 
        {
            for (int c = 0; c < Decompressed_Matrix[0].length; c++) 
            {
                Decompressed_Matrix[r][c]=Integer.parseInt(matrix[r][c]) ;
            }
        }
        
            Vector_Node vector = new Vector_Node();
            vector.setQ(Quantizer_Str.get(i).getQ());
            vector.setQ_1_Block(Decompressed_Matrix);
            Quantizer.add(vector);

        }
    
    
                      
//        for (int[] row : mat) {
//            System.out.println(Arrays.toString(row));
//        }



//******************DECOMPRESSION********************************************************

 LinkedList<int[][]> Constructed_Blocks = new LinkedList<>();
 
            for (int i = 0; i < codearr.length; i++) 
            {
                for (int j = 0; j < Quantizer.size(); j++) 
                {
                    if (Integer.parseInt(codearr[i])==Quantizer.get(j).getQ())
                    {
                        Constructed_Blocks.add(Quantizer.get(j).getQ_1_Block());
                        break;
                    }
                    
                }
            }
//********************************************************************************
            

       int decompressed_image[][]= Construct_Image(Constructed_Blocks, Imagerow, imagecol);
       
       writeImage(decompressed_image, destination);
        JOptionPane.showMessageDialog(null,"File Decompressed Successfully!");

    }
    
     
    public static int[][] padding(String Path, int blockrow , int blockcol)
    {
        

        int[][] readimage =readImage(Path);

//================================ORG Dim========================================
        int orgrow = readimage.length;
        int orgcol = readimage[0].length;
        int orgsize = orgrow * orgcol;

      
        int blocksize = blockrow * blockcol;
//================================Handeling padding========================================
        double rowratio = orgrow / blockrow;
        double colratio = orgcol / blockcol;
        
        int fixedrow = 0;
        int fixedcol = 0;

        if (orgrow % blockrow != 0) 
        {
          //  fixedrow = (int) (rowratio + 1);
            orgrow= ((int)rowratio) * (blockrow);
        } 
        
        else 
        {
            fixedrow = (int) rowratio;
        }

        if (orgcol % blockcol != 0) 
        {
            //fixedcol = (int) (colratio + 1);
            orgcol=((int)colratio) * (blockcol);
        } 
        else 
        {
            fixedcol = (int) colratio;
        }

        int padddedrow = fixedrow * blockrow;
        int paddedcol = fixedcol * blockcol;

        int paddedsize = padddedrow * paddedcol;
        int noBlocks = paddedsize / blocksize;

        int avgpadd = 0;
//================================Filling Padding========================================
        for (int i = 0; i < orgrow; i++) {
            for (int j = 0; j < orgcol; j++) {

                avgpadd += readimage[i][j];
            }
        }

        avgpadd = avgpadd / orgsize;

        int[][] padded = new int[padddedrow][paddedcol];

        for (int r = 0; r < padddedrow; r++) {
            for (int c = 0; c < paddedcol; c++) {
                if (r >= orgrow || c >= orgcol) {

                    padded[r][c] = avgpadd;

                } 
                else 
                {
                    padded[r][c] = readimage[r][c];
                }

            }
        }
        
        
        int [][] modified = new int [orgrow][orgcol];
        
        for (int r = 0; r < orgrow; r++) {
            for (int c = 0; c < orgcol; c++) {
                
               modified[r][c]=readimage[r][c];
            }
        }


       return modified;
    }
    
 public static LinkedList<int[][]> Crop_Image(int [][] padded, int blockrow,int blockcol){        
int paddedcol=padded[0].length;
int padddedrow=padded.length;
//==================================Splitting into Array of 2d matrices=========
LinkedList<int[][]> listOfBlocks = new LinkedList<int[][]>();
int Filling_Row = 0;
int Filling_Coll = 0;
while(true){
         if(Filling_Coll == paddedcol){
             Filling_Row+=blockrow;
             Filling_Coll=0;
         }   
         if(Filling_Row == padddedrow){
             break;
         }
         int block[][] = new int[blockrow][blockcol];
         for (int i = Filling_Row , br = 0; i < blockrow+Filling_Row ; i++ , br++) {
             for (int j = Filling_Coll , bc = 0; j < blockcol+Filling_Coll; j++ , bc++) {
                 block[br][bc] = padded[i][j];
             }
    }
         Filling_Coll+=blockcol;
         listOfBlocks.add(block);
         
        }
    return listOfBlocks;
    }
        
        
public static int [][] Construct_Image ( LinkedList<int [][]> ListOfBlocks, int imagerow, int imagecol)
{
    
   int [][] transformed_matrix = new int [imagerow][imagecol];
   
   int blockrow=ListOfBlocks.get(0).length;
   int blockcol=ListOfBlocks.get(0)[0].length;
   
   int paddedcol=imagecol;
   int padddedrow=imagerow;
   
      
int Filling_Row = 0;
int Filling_Col = 0;
int k =0;

            
while(true){
    
         if(Filling_Col == paddedcol){
             Filling_Row+=blockrow;
             Filling_Col=0;
         }   
         if(Filling_Row == padddedrow || k==ListOfBlocks.size() ){
             break;
         }
         int [][] block =ListOfBlocks.get(k);
         for (int i = Filling_Row , br = 0; i < blockrow+Filling_Row ; i++ , br++) {
             for (int j = Filling_Col , bc = 0; j < blockcol+Filling_Col; j++ , bc++) {
                 transformed_matrix[i][j]= block[br][bc] ;
             }
             }
         Filling_Col+=blockcol;
         k++;
         
        }

   
   return transformed_matrix;
}
   
}