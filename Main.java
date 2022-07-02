import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileInputStream;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Files;
import javax.sound.sampled.*;
import java.awt.GradientPaint;
import java.awt.BasicStroke;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

public class Main{
    public static File folder = new File("./songs");
    public static File[] listOfFiles = folder.listFiles();
    public static String[] songNames = new String[listOfFiles.length];
    public static SongSelection songs;
    public static FileInputStream stream;
    public static MouseMovement mouseMove = new MouseMovement();
    public static Frame frame = new Frame("Java Audio Player", 1280, 720);
    public static Panel panel = new Panel();
    public static Color fgColor = new Color(0, 118, 199);
    public static Color fgColor2 = new Color(0, 179, 78);
    public static int mouseX = 0;
    public static int mouseY = 0;
    public static AudioPlayer audio = new AudioPlayer();
    public static boolean click = false;
    public static boolean release = false;
    public static boolean right = false;
    public static boolean left = false;
    public static GradientPaint bg = new GradientPaint(0, 0, fgColor, frame.WIDTH, frame.HEIGHT, fgColor2);
    public static void main(String[] args){
        //Reads the "songs" folder to get all of the songs in the list
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
                songNames[i] = listOfFiles[i].getName();
            } else if (listOfFiles[i].isDirectory()) {
                System.out.println("Directory " + listOfFiles[i].getName());
            }
        }

        songs = new SongSelection(songNames);
        frame.add(panel);
        frame.validate();
        long lastTick = System.currentTimeMillis();
        char colors[][] = {{0, 118, 199}, {0, 179, 78}, {179, 0, 121}, {83, 0, 199}};
        while (true){
            if (System.currentTimeMillis() > lastTick){
                frame.repaint();
                frame.revalidate();
                lastTick+= 16;
            }
        }
    }
}

class Frame extends JFrame{
    public static String TITLE;
    public static int WIDTH;
    public static int HEIGHT;
    Frame(String title, int w, int h){
        TITLE = title;
        WIDTH = w;
        HEIGHT = h;
        this.setTitle(TITLE);
        this.setSize(WIDTH, HEIGHT);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.addMouseListener(new MouseControl());
        this.addMouseMotionListener(Main.mouseMove);
        this.addKeyListener(new Keyboard());
        this.setResizable(false);
    }
}

class Panel extends JPanel{
    int fileNumber = 0;
    public static double nobPos = 0;
    public static double lastNobPos = 0;
    public static boolean nobHold = false;
    public static boolean selected = false;
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Font font = new Font("Verdana", Font.BOLD, 24);
        renderSongList(g2d, Main.songs);
    }

    public void renderSongList(Graphics2D tG, SongSelection tS){
        int headerHeight = 100;
        int offset = 100;
        int height = 150;
        int minutes = 0;
        int seconds = 0;
        Font font = new Font("Verdana", Font.BOLD, (height / 4));
            tG.setPaint(Main.bg);
            tG.fillRect(0, 0, Main.frame.WIDTH - 8, Main.frame.HEIGHT - 31);
        tG.setFont(font);
        for (int i = 0; i < tS.songList.length; i++){
            tG.setColor(new Color(255, 255, 255, 64));
            tG.fillRoundRect(((Main.frame.WIDTH / 64) - 8), ((i * height) + offset), ((Main.frame.WIDTH - 64) - (Main.frame.WIDTH / 32)), (height - 10), 40, 40);
            if (!nobHold){
            if (Main.mouseY > ((i * height) + offset) && Main.mouseY < (((((i + 1) * height) + offset) - 1) - 10) && Main.mouseX > ((Main.frame.WIDTH / 64) - 8) && Main.mouseX < ((Main.frame.WIDTH - 64) - (Main.frame.WIDTH / 32)) && Main.mouseY < (Main.frame.HEIGHT - 150)){
                tG.setColor(new Color(255, 255, 255, 64));
                tG.fillRoundRect(((Main.frame.WIDTH / 64) - 8), ((i * height) + offset), ((Main.frame.WIDTH - 64) - (Main.frame.WIDTH / 32)), (height - 10), 40, 40);
                if (Main.click && !selected){
                    selected = true;
                    tG.setColor(new Color(0, 0, 0, 128));
                    tG.fillRoundRect(((Main.frame.WIDTH / 64) - 8), ((i * height) + offset), ((Main.frame.WIDTH - 64) - (Main.frame.WIDTH / 32)), (height - 10), 40, 40);
                    Main.audio.endPlayback();
                    fileNumber = i;
                    try{
                        Main.audio.loadFile(Main.listOfFiles[i]);
                    }catch (UnsupportedAudioFileException | IOException | LineUnavailableException e){
        
                    }
                    Main.audio.playFromBeginnning();
                }
            }
        }
        if (!Main.click){
            selected = false;
        }
            tG.setColor(Color.BLACK);
            tG.setStroke(new BasicStroke(3));
            tG.drawRoundRect(((Main.frame.WIDTH / 64) - 8), ((i * height) + offset), ((Main.frame.WIDTH - 64) - (Main.frame.WIDTH / 32)), (height - 10), 40, 40);
            tG.setColor(Color.WHITE);
            tG.drawString(tS.songList[i], (((Main.frame.WIDTH / 64) - 8) + ((Main.frame.WIDTH / 64))), ((((i * height) + offset) + (height / 64)) + font.getSize()));
            minutes = (int) (((Main.listOfFiles[i].length() / 48000) / 60) / 4);
            seconds = (int) (((Main.listOfFiles[i].length() - (((minutes * 48000) * 60) * 4)) / 48000) / 4);
            if (seconds < 10){
                tG.drawString("Playback Time: "+String.valueOf(minutes)+":0"+String.valueOf(seconds), (((Main.frame.WIDTH / 64) - 8) + ((Main.frame.WIDTH / 64))), ((((i * height) + offset) + (height / 64)) + (font.getSize() * 3)));
            }else{
                tG.drawString("Playback Time: "+String.valueOf(minutes)+":"+String.valueOf(seconds), (((Main.frame.WIDTH / 64) - 8) + ((Main.frame.WIDTH / 64))), ((((i * height) + offset) + (height / 64)) + (font.getSize() * 3)));
            }
        }
        int seekSeconds = 0;
        tG.setColor(new Color(0, 0, 0, 128));
        tG.fillRect(0, 0, Main.frame.WIDTH, headerHeight - 20);
        tG.setColor(new Color(0, 0, 0, 128));
        tG.fillRect(0, (((Main.frame.HEIGHT - 31) - headerHeight) - 20), Main.frame.WIDTH, Main.frame.HEIGHT - 31);
        tG.setColor(Color.WHITE);
        tG.setColor(new Color(255, 255, 255, 128));
        tG.setStroke(new BasicStroke(10));
        tG.drawLine((Main.frame.WIDTH / 4), ((Main.frame.HEIGHT - 31) - 30), ((Main.frame.WIDTH / 4) + (Main.frame.WIDTH / 2)), ((Main.frame.HEIGHT - 31) - 30));
        if (Main.audio.clip != null){
            minutes = (int) ((Main.audio.clip.getFramePosition() / 48000) / 60);
            seconds = (int) ((Main.audio.clip.getFramePosition() - ((minutes * 48000) * 60)) / 48000);
            if (seconds < 10){
                tG.drawString(String.valueOf(minutes)+":0"+String.valueOf(seconds), ((Main.frame.WIDTH / 1.25f) - 32), ((Main.frame.HEIGHT - 31) - 20));
            }else{
                tG.drawString(String.valueOf(minutes)+":"+String.valueOf(seconds), ((Main.frame.WIDTH / 1.25f) - 32), ((Main.frame.HEIGHT - 31) - 20));
            }
            if (nobHold){
                nobPos = (double) (((Main.mouseX) - (Main.frame.WIDTH / 4)) / (((double) Main.frame.WIDTH) / 2));
                if (nobPos > 1){
                    nobPos = 1;
                }
                if (nobPos < 0){
                    nobPos = 0;
                }
                if (nobPos != lastNobPos){
                    lastNobPos = nobPos;
                    Main.audio.seek((int) (nobPos * ((((double) Main.listOfFiles[fileNumber].length()) / 4) / 48000)));
                }else{
                    Main.audio.pause();
                }
                if (!Main.click){
                    Main.audio.seek((int) (nobPos * ((((double) Main.listOfFiles[fileNumber].length()) / 4) / 48000)));
                    nobHold = false;
                }
            }else{
                if (Main.left){
                    Main.left = false;
                    seekSeconds = (((minutes * 60) + seconds) - 5);
                    if (seekSeconds < 0){
                        seekSeconds = 0;
                    }
                    Main.audio.seek(seekSeconds);
                }else if (Main.right){
                    Main.right = false;
                    seekSeconds = (((minutes * 60) + seconds) + 5);
                    if (seekSeconds > ((Main.listOfFiles[fileNumber].length() / 4) / 48000)){
                        seekSeconds = (int) ((Main.listOfFiles[fileNumber].length() / 4) / 48000);
                    }
                    Main.audio.seek(seekSeconds);
                }
                nobPos = (double) (Main.audio.clip.getFramePosition() / (((double) Main.listOfFiles[fileNumber].length()) / 4));
            if (Main.mouseX > (Main.frame.WIDTH / 4) && Main.mouseX < ((Main.frame.WIDTH / 4) + (Main.frame.WIDTH / 2)) && Main.mouseY > (((Main.frame.HEIGHT - 31) - 30) - 10) && Main.mouseY < (((Main.frame.HEIGHT - 31) - 30) + 10)){
                if (Main.click){
                    nobHold = true;
                    Main.audio.pause();
                }
            }
        }
            tG.setColor(Color.WHITE);
            tG.drawLine((Main.frame.WIDTH / 4), ((Main.frame.HEIGHT - 31) - 30), (int) ((Main.frame.WIDTH / 4) + ((Main.frame.WIDTH / 2) * nobPos)), ((Main.frame.HEIGHT - 31) - 30));
            tG.fillOval((int) (((Main.frame.WIDTH / 4) + ((Main.frame.WIDTH / 2) * nobPos)) - 10), (((Main.frame.HEIGHT - 31) - 30) - 10), 20, 20);
        }else{
            tG.drawString("0:00", ((Main.frame.WIDTH / 1.25f) - 32), ((Main.frame.HEIGHT - 31) - 20));
        }
        tG.setColor(Color.WHITE);
        tG.drawString("Java Audio Player", 32, font.getSize() + 16);
    }
}

class Banner{
    public static String myName;
    Banner(String name){
        myName = name;
    }
}

class SongSelection{
    public static String[] songList;
    SongSelection(String[] songL){
        songList = songL;
    }
}

class MouseControl implements MouseListener{

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e){
        if (e.getButton() == 1){
            Main.click = false;
        }
    }

    @Override
    public void mouseExited(MouseEvent e){

    }

    @Override
    public void mousePressed(MouseEvent e){
        if (e.getButton() == 1){
            Main.click = true;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e){
        
    }
}   

class MouseMovement implements MouseMotionListener{

    @Override
    public void mouseDragged(MouseEvent e){
        Main.mouseX = (e.getX() - 8);
        Main.mouseY = (e.getY() - 31);
    }

    @Override
    public void mouseMoved(MouseEvent e){
        Main.mouseX = (e.getX() - 8);
        Main.mouseY = (e.getY() - 31);
    }
}

class AudioPlayer{
    public static File audioFile;
    public static AudioInputStream audioInputStream;
    public static Clip clip;
    public static int lastFrame = 0;
    public void loadFile(File file) throws UnsupportedAudioFileException, IOException, LineUnavailableException{
        if (clip == null){
            clip = AudioSystem.getClip();
        }
        clip.stop();
        clip.close();
        clip.flush();
        audioFile = file;
        audioInputStream = AudioSystem.getAudioInputStream(audioFile);
        clip.open(audioInputStream);
    }

    public void playFromBeginnning(){
        clip.setFramePosition(0);
        clip.start();
    }

    public void endPlayback(){
        
    }

    public void pause(){
        lastFrame = clip.getFramePosition();
        clip.stop();
    }

    public void resume(){
        clip.setFramePosition(lastFrame);
        clip.start();
    }

    public void seek(float seconds){
        int tempFrame = (int) (seconds * 48000);
        clip.stop();
        clip.setFramePosition(tempFrame);
        clip.start();
    } 
}

class Keyboard implements KeyListener{
    @Override
    public void keyPressed(KeyEvent e){
        if (e.getKeyCode() == 39){
            Main.right = true;
        }
        if (e.getKeyCode() == 37){
            Main.left = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e){

    }

    @Override
    public void keyTyped(KeyEvent e){

    }
}