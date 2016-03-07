package com.suku.salauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by muarda on 01/03/2016.
 */
public class saLauncher extends Activity {
    private PackageManager pm;
    private String[] labels;
    private String[] names;
    private String list;
    private int itemCount = 0;
    private boolean init = false;

    private String getPackageName(String str) {
        if (!init)
            return "";

        for (int i = 0; i < itemCount -1; i++) {
            if (str.matches(labels[i].toLowerCase()))
                return names[i];
        }

        return str;
    }

    private boolean saveData(String fName, String data) {
        File f = new File(this.getFilesDir(), fName);
        FileManager.writeToFile(f, data);
        return true;
    }

    private String loadData(String fName) {
        File f = new File(this.getFilesDir(), fName);
        return FileManager.readFromFile(f);
    }

    private boolean init() {
        pm = getPackageManager();

        boolean fileExist = true;
        File file = new File(this.getFilesDir(), getString(R.string.sa_lbl_list));
        if (!file.exists())
            fileExist = false;
        else {
            file = new File(this.getFilesDir(), getString(R.string.sa_pkg_list));
            if (!file.exists())
                fileExist = false;
        }

        if (fileExist) {
            labels = loadData(getString(R.string.sa_lbl_list)).split("/");
            names = loadData(getString(R.string.sa_pkg_list)).split("/");
            itemCount = labels.length;
        } else {
            update();
        }

        return true;
    }

    private boolean update() {
        String tmpLabels = "";
        String tmpNames = "";
        String[] tmpLabelsArray;
        String[] tmpNamesArray;
        int tmpComparator;

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        itemCount = 0;
        List<ResolveInfo> activities = pm.queryIntentActivities(i, 0);
        for(ResolveInfo ri:activities) {
            itemCount++;
            tmpLabels += ri.loadLabel(pm) + "/";
            tmpNames += ri.activityInfo.packageName + "/";
        }

        labels = tmpLabels.split("/");
        names = new String[itemCount - 1];

        tmpLabelsArray = tmpLabels.split("/");
        tmpNamesArray = tmpNames.split("/");

        Arrays.sort(labels, String.CASE_INSENSITIVE_ORDER);
        for(int j = 0; j < itemCount - 1; j++) {
            tmpComparator = 1;
            for (int k = 0; k < itemCount && (tmpComparator != 0); k++) {
                tmpComparator = labels[j].compareTo(tmpLabelsArray[k]);
                if (tmpComparator == 0) {
                    names[j] = new String(tmpNamesArray[k]);
                }
            }
        }

        list = getList();

        saveData(getString(R.string.sa_lbl_list), tmpLabels);
        saveData(getString(R.string.sa_pkg_list), tmpNames);

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EditText cmd;
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sa_launcher);
        cmd = (EditText) findViewById(R.id.sa_command);
        cmd.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER))
                    return execCommand(v);
                return false;
            }
        });


        init = init();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void updateConsole(String str) {
        TextView text = (TextView) findViewById(R.id.sa_monitor);
        text.setText(str);
    }

    private String getList() {
        String tmp = "";

        for (int i = 0; i < itemCount - 1; i++) {
            tmp += labels[i] + " : " + names[i] + "\n";
        }

        return tmp;
    }

    public boolean execCommand(View v) {
        EditText cmd = (EditText)findViewById(R.id.sa_command);
        String[] sCmd = cmd.getText().toString().toLowerCase().split(" ");
        int argCount = sCmd.length;

        switch (sCmd[0]) {
            case "h":case "help": {
                if (argCount == 1) {
                }
            }
            case "list": {
                list = getList();
                updateConsole(list);
            }
            case "set": {

            }
            case "update": {
                update();
            }
            default: {
                try {
                    sCmd[0] = getPackageName(cmd.getText().toString());
                    pm.getPackageInfo(sCmd[0], PackageManager.GET_ACTIVITIES);
                    Intent i = pm.getLaunchIntentForPackage(sCmd[0]);
                    this.startActivity(i);
                } catch (PackageManager.NameNotFoundException e) {

                }
            }
        }

        cmd.setText("");
        return true;
    }
}
