package org.insilico.build

import aQute.bnd.gradle.Export
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

import java.util.jar.Attributes
import java.util.jar.JarInputStream
import java.util.jar.Manifest
import java.util.regex.Matcher
import java.util.regex.Pattern

class ExportApplication extends DefaultTask{
    public ExportApplication(){
}
    @TaskAction
    public void exportApplication(){
        File newFolder = new File("build");
        boolean created =  newFolder.mkdir();
        BufferedWriter writer = new BufferedWriter(new FileWriter("app.bndrun"))
        writer.write("-runfw:org.eclipse.osgi;version=3.13.200\n")
        Pattern p = Pattern.compile("(\\d+\\.)+jar")
        String ans=""
        String version=""
        String name=""
        for(File a: this.getProject().configurations.osgiInstall.resolvedConfiguration.getFiles()) {
            version = ""
            Matcher m = p.matcher(a.name);
            println(a.name)
            JarInputStream jarStream = new JarInputStream(new FileInputStream(a));
            Manifest mf = jarStream.getManifest();
            Attributes attributes = mf.getMainAttributes();
            name = attributes.getValue("Bundle-SymbolicName");
            if (name != "org.eclipse.osgi; singleton:=true" && name != null) {
                if (m.find()) {
                    version = m.group()
                    version = version.substring(0, version.length() - 4)
                    System.out.println(version);
                }
                ans += name + ";";
                if (version.length() > 0)
                    ans += "version=" + version
                ans += ",\\\n"
            }
        }
        writer.write("-runbundles:"+ans)
        writer.close()
        Task createFatJarTask=this.getProject().task('edcrfv',type:Export).configure{
            bndrun 'app.bndrun'
        }
        this.project.tasks.edcrfv.export();
    }
}
