import java.util.*;
import java.io.*;

public class main{
  public static void main(String[] args){
    Scanner scan=new Scanner(System.in);

    //Names of wavelengths
    String nameDRed="DeepRed";
    String nameBRed="BrightRed";
    String nameEGreen="EmeraldGreen";
    String nameTGreen="TGreen";
    String nameRBlue="RoyalBlue";
    String nameViolet="Violet";
    String names[]=new String[6];
    names[0]=nameDRed;
    names[1]=nameBRed;
    names[2]=nameEGreen;
    names[3]=nameTGreen;
    names[4]=nameRBlue;
    names[5]=nameViolet;

    //Wavelengths and Uncertainties
    double waves[]=new double[6];
    double uWaves[]=new double[6];
    waves[0]=659e-9;  uWaves[0]=10e-9;
    waves[1]=612e-9;  uWaves[1]=15e-9;
    waves[2]=516e-9;  uWaves[2]=20e-9;
    waves[3]=502e-9;  uWaves[3]=15e-9;
    waves[4]=440e-9;  uWaves[4]=10e-9;
    waves[5]=415e-9;  uWaves[5]=10e-9;


    //Uncertainties in intensity
    double unInI[][]=new double[6][3];
    unInI[0][0]=0.008;  unInI[0][1]=0.004;  unInI[0][2]=0.007;
    unInI[1][0]=0.002;  unInI[1][1]=0.002;  unInI[1][2]=0.011;
    unInI[2][0]=0.006;  unInI[2][1]=0.006;  unInI[2][2]=0.004;
    unInI[3][0]=0.014;  unInI[3][1]=0.009;  unInI[3][2]=0.045;
    unInI[4][0]=0.012;  unInI[4][1]=0.007;  unInI[4][2]=0.005;
    unInI[5][0]=0.007;  unInI[5][1]=0.028;  unInI[5][2]=0.013;

    //Uncertainty in Temp
    double unInT=0.01;

    List<Double> temp=new ArrayList();
    List<Double> intensity=new ArrayList();
    List<String> wavelength=new ArrayList();
    List<Double> lamda=new ArrayList();
    List<Double> tc=new ArrayList();
    List<Double> intensityZero=new ArrayList();

    List<Double> lnlnFunc=new ArrayList();
    List<Double> lnDT=new ArrayList();
    List<Double> lnLamda=new ArrayList();

    List<Double> uncertaintyLamda=new ArrayList();
    List<Double> uncertaintyDT=new ArrayList();
    List<Double> uncertaintylnln=new ArrayList();



    //wavelength

    List<Integer> sizes=new ArrayList();

    List<Point> points=new ArrayList();

    int y = 0;
    double starter = 0;
    String line = null;
    //Read
    for(int i=0;i<6;i++){
      for(int j=0;j<3;j++){
        //Setting file to read
        String type = "A";

        switch(j){
          case 0: type="A"; break;
          case 1: type="B"; break;
          case 2: type="C"; break;
        }

        String fileName = "Data/" + names[i] + type + "100ms.csv";
        System.out.println("Reading " + names[i] + type);

        int x = 0;

        try{
          FileReader fileReader = new FileReader(fileName);
          BufferedReader br = new BufferedReader(fileReader);

          while((line = br.readLine()) != null){
            String[] values = line.split(",");

            temp.add(Double.parseDouble(values[0]));
            intensity.add(Double.parseDouble(values[1]));
            wavelength.add(names[i] + type);


            if(x == 0){
              starter = Double.parseDouble(values[1])+0.05;
            }
            intensityZero.add(starter);

            lnlnFunc.add( Math.log( - Math.log( Double.parseDouble( values[1] ) / starter ) ) );
            uncertaintylnln.add(Math.pow(
                                        (1.0/starter)*(1.0/starter)
                                                     +
                     (1/Double.parseDouble(values[1])*(1/Double.parseDouble(values[1])))

                                         ,0.5)       *
                                                    (1/
                          Math.log(starter/Double.parseDouble(values[1])))*unInI[i][j]);

            lamda.add(waves[i]);

            lnLamda.add(Math.log(waves[i]));

            uncertaintyLamda.add(uWaves[i]/waves[i]);

            x++;
          }
        }catch(Exception ex){
          System.out.println(ex);
        }
        sizes.add(x);
        System.out.println(names[i]+type+" has "+x+" entries, spanning from index "+(y)+" to "+(y+x-1));
        y+=x;
        for(int k=x;k>0;k--){
          //Sets last temp as critical temp
          tc.add(temp.get(y-1)-0.001);
          lnDT.add(Math.log(temp.get(y-k)-temp.get(y-1)+0.001));

          uncertaintyDT.add(Math.pow((1/temp.get(y-k))*(1/temp.get(y-k))
                                      +(1/tc.get(y-k))*(1/tc.get(y-k)),0.5)*unInT);

          //System.out.println(temp.get(y-k)+";"+intensity.get(y-k)+";"+tc.get(y-k)+";"+intensityZero.get(y-k)+";"+lnlnFunc.get(y-k)+";"+lnDT.get(y-k)+";"+wavelength.get(y-k)+";");
        }
        //System.out.println(x);
        //System.out.println(tc.get(y-1));



      }
    }
    //Write
    System.out.println(temp.size());
    try{
      FileWriter write = new FileWriter("CollatedData.csv", false);
      PrintWriter print_line = new PrintWriter(write);
      //String currentW="DeepRedA";
      //double min=99;
      //double max=0;                     0             1                 2           3               4                 5                 6               7             8                   9                         10                              11
      print_line.printf( "%s" + "%n" , "Wavelength name,Temperature/C,Intensity/V,Wavelength/m,Zero Intensity/V,Critical Temperature/C,ln(DeltaT)/AU,ln(Lamda)/AU,ln(ln(I0overI))/AU,Uncertainty in ln(Lamda)/AU,Uncertainty in ln(DeltaT)/AU,Uncertainty in ln(ln(I0overI))/AU");
      int p=0;
      for(int i=0;i<y;i++){
        boolean validPoint=!Double.isNaN(lnlnFunc.get(i)+lnDT.get(i)+lnLamda.get(i))&&(uncertaintyDT.get(i)<7)&&(uncertaintylnln.get(i)<7)&&(uncertaintyLamda.get(i)<7);
        if(validPoint){
          print_line.printf( "%s" + "%n" , wavelength.get(i)   + "," +
          temp.get(i)             + "," + intensity.get(i)     + "," +
          lamda.get(i)            + "," + intensityZero.get(i) + "," +
          tc.get(i)               + "," + lnDT.get(i)          + "," +
          lnLamda.get(i)          + "," + lnlnFunc.get(i)      + "," +
          uncertaintyLamda.get(i) + "," + uncertaintyDT.get(i) + "," +
          uncertaintylnln.get(i));
          p++;
          if(!wavelength.get(i).equals(wavelength.get(i+1))){
            System.out.println(wavelength.get(i)+" ended, "+(p+1)+" is the termination point");
          }
        }

      }
      System.out.println(p);
      print_line.close();
    }catch(Exception ex){
      System.out.println(ex);
    }
    try{
      FileWriter write = new FileWriter("ConstDT101.csv", false);
      PrintWriter print_line = new PrintWriter(write);
      //String currentW="DeepRedA";
      //double min=99;
      //double max=0;                       0             1                 2           3               4                 5                 6               7             8                   9                         10                              11
      //print_line.printf( "%s" + "%n" , "Wavelength name,Temperature/C,Intensity/V,Wavelength/m,Zero Intensity/V,Critical Temperature/C,ln(DeltaT)/AU,ln(Lamda)/AU,ln(ln(I0overI))/AU,Uncertainty in ln(Lamda)/AU,Uncertainty in ln(DeltaT)/AU,Uncertainty in ln(ln(I0overI))/AU");
      int p=0;
      for(int i=0;i<y;i++){
      boolean validPoint=!Double.isNaN(lnlnFunc.get(i)+lnDT.get(i)+lnLamda.get(i))&&(uncertaintyDT.get(i)<7)&&(uncertaintylnln.get(i)<7)&&(uncertaintyLamda.get(i)<7);
      validPoint=validPoint&&(temp.get(i)-tc.get(i))>=0.100&&(temp.get(i)-tc.get(i))<=0.102;
      //System.out.println(temp.get(i)-tc.get(i));
        if(validPoint){
          print_line.printf( "%s" + "%n" , /*wavelength.get(i)   + "," +
          temp.get(i)             + "," + intensity.get(i)     + "," +
          lamda.get(i)            + "," + intensityZero.get(i) + "," +
          tc.get(i)               + "," + lnDT.get(i)          + "," +*/
          lnLamda.get(i)          + "," + lnlnFunc.get(i)      + "," +
          /*uncertaintyLamda.get(i) + "," + uncertaintyDT.get(i) + "," +*/
          uncertaintylnln.get(i));
          p++;
        }

      }
      System.out.println(p);
      print_line.close();
    }catch(Exception ex){
      System.out.println(ex);
    }

  }
}
