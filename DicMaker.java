import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Properties;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.btree.BTree;
import jdbm.helper.StringComparator;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;


public class DicMaker {
  static String BTREE_NAME = "skk_dict";

  static public void main(String argv[]) {
    
    
    RecordManager recman;
    long          recid;
    Tuple         tuple = new Tuple();
    TupleBrowser  browser;
    BTree         tree;
    Properties    props;

    props = new Properties();

    try {
      // open database and setup an object cache
      recman = RecordManagerFactory.createRecordManager("C:/skk_dict_btree", props );

      // try to reload an existing B+Tree
      recid = recman.getNamedObject( BTREE_NAME );
      if ( recid != 0 ) {
        tree = BTree.load( recman, recid );
        System.out.println( "Reloaded existing BTree with " + tree.size()
            + " famous people." );
      } else {
        // create a new B+Tree data structure and use a StringComparator
        // to order the records based on people's name.
        tree = BTree.createInstance( recman, new StringComparator() );
        recman.setNamedObject( BTREE_NAME, tree.getRecid() );
        System.out.println( "Created a new empty BTree" );
      }

      // insert people with their respective occupation

      InputStreamReader fr = null;  
      BufferedReader br = null;

      FileInputStream fis = new FileInputStream("C:/android-sdk-windows-1.5_r1/tools/skk_L_dic_for_android_sorted.utf8");
      fr = new InputStreamReader(fis, "UTF-8");
      br = new BufferedReader(fr);

      int c = 0;

      for (String line = br.readLine(); line != null; line = br.readLine()) {
        if (line.startsWith(";;")) continue;

        int idx = line.indexOf(' ');
        if (idx == -1) continue;
        String key = line.substring(0, idx);
        String value = line.substring(idx + 1, line.length());

        tree.insert( key, value, true );

        if (++c % 1000 == 0) {
          recman.commit();
        }
      }

      // make the data persistent in the database
      recman.commit();

    } catch ( Exception e ) {
      throw new RuntimeException(e);
    }
  }
}
