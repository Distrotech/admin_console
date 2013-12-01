import java.security.*;

class NetworkSentrySerial {
  boolean valid=false;
  String nsversion="3.5";
  String SerialKey="";
  public NetworkSentrySerial(String serkeyin,String hname,String ipaddr) {
    String serkeyout="";
    String[] serkey;
    int sercnt,hord,lord,sticky,idnum,idrem;
    String digest,keyname,keyout;
    SerialKey=serkeyin;
    byte[] digout;

    MessageDigest keymd5;

    if (serkeyin.length() == 44) {
      serkey=serkeyin.split("-");    
      for(sercnt=0;sercnt < 7;sercnt ++) {
        serkeyout=serkeyout.concat(serkey[sercnt]);
      }
      digest=serkeyout.substring(6,38);

      try {
        hord=Integer.parseInt(serkeyout.substring(0,2),16);
        lord=Integer.parseInt(serkeyout.substring(2,4),16);
        sticky=Integer.parseInt(serkeyout.substring(4,6),16);
      } catch (NumberFormatException e) {
        hord=0;
        lord=0;
        sticky=0;
      }

      idnum=hord*256+lord;
    
      if (sticky >= 128) {
        keyname=hname+" "+ipaddr+" x";
        sticky=sticky-128;
      } else {
        keyname=ipaddr+" "+hname+" x";
      }
      keyname=keyname.toLowerCase();

      if (sticky >= 64) {
        idrem=idnum % 9;
        keyname=keyname+" "+idrem;
        sticky=sticky-64;
      }

      if (sticky >= 32) {
        sticky=sticky-32;
        keyname=keyname.replace('i','^');
      }

      if (sticky >= 16) {
        sticky=sticky-16;
        keyname=keyname.replace('.','%');
      }

      if (sticky >= 8) {
        sticky=sticky-8;
        keyname=keyname.replace('a','$');
      }

      if (sticky >= 4) {
        sticky=sticky-4;
        keyname=keyname.replace('o','#');
      }

      if (sticky >= 2) {
        sticky=sticky-2;
        keyname=keyname.replace('e','@');
      }

      if (sticky >= 1) {
        keyname=keyname.replace('u','!');
      }

      keyname=idnum+nsversion+keyname;


      try {
        keymd5=MessageDigest.getInstance("MD5");
        keymd5.update(keyname.getBytes());
        digout=keymd5.digest();
        keyout=new String(digout);
      } catch (NoSuchAlgorithmException e) {
        System.out.println("NoSuchAlgorithmException");
        keyout="";
      }


      digest=unhex(digest);


      if (keyout.equals(digest)) {
        valid=true;
      }
    }
  }

  public String isValid() {
    if (valid) {
      return "All In Order Captain My Captain";
    } else {
//      return "All In Order Captain My Captain";
      return "Not Valid";
    }
  }
  private static String unhex(String orighash) {
    String hashout="";
    byte[] temphash;
    int charval;

    temphash=new byte[16];

    for(int charcnt=0;charcnt < 16;charcnt++) {
       try {
        temphash[charcnt]=(byte)Integer.parseInt(orighash.substring(charcnt*2,charcnt*2+2),16);
      } catch (NumberFormatException e) {
        temphash[charcnt]=0;
      }
    }

    hashout=new String(temphash);
    return hashout;
  }  
  public String toString(){
    return SerialKey;
  }
}
