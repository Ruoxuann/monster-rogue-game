import java.io.*;

class In {
    private BufferedReader br;

    public In(String filePath) {
        try {
            FileReader fileReader = new FileReader(filePath);
            br = new BufferedReader(fileReader);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public String readLine() {
        String s = null;
        try {
            s = br.readLine();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return s;
    }

    public void close() {
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
