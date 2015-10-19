// ----------------------------------------------------------------------------
// Copyright 2007-2015, GeoTelematic Solutions, Inc.
// All rights reserved
// ----------------------------------------------------------------------------
//
// This source module is PROPRIETARY and CONFIDENTIAL.
// NOT INTENDED FOR PUBLIC RELEASE.
// 
// Use of this software is subject to the terms and conditions outlined in
// the 'Commercial' license provided with this software.  If you did not obtain
// a copy of the license with this software please request a copy from the
// Software Provider.
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Description:
//  Remote Log Query Client (EXPERIMENTAL)
// ----------------------------------------------------------------------------
// Change History:
//  2012/12/24  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.extra.util;

import java.io.*;
import java.util.*;
import java.net.*;
import javax.net.*;

import org.opengts.util.*;

/**
*** Remote Log client
**/

public class RemoteLogClient
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Private Constructor
    **/
    private RemoteLogClient()
    {
        super();
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------
    
    private static StringBuffer GlobalBuffer  = new StringBuffer();
    private static int          GlobalCapture = 0;

    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
        int    port = RTConfig.getInt(   "port",0);
        String host = RTConfig.getString("host","localhost");

        /* start listener thread */
        //ClientSocketThread.setLineTerminatorChar('\r');
        ClientSocketThread cst = new ClientSocketThread(host, port) {
            protected void handleMessage(String msg) {
                if (msg.startsWith(RemoteLogServer.LOG_OUTPUT_BEGIN)) {
                    // start capture, remove prefixing <LogOutput> header
                    GlobalCapture++; // start capture
                    msg = msg.substring(RemoteLogServer.LOG_OUTPUT_BEGIN.length());
                }
                boolean capture = (GlobalCapture > 0)? true : false;
                if (msg.endsWith(RemoteLogServer.LOG_OUTPUT_END)) {
                    // end capture, remove trailing </LogOutput> footer
                    msg = msg.substring(0,msg.length() - RemoteLogServer.LOG_OUTPUT_END.length());
                    GlobalCapture = 0;
                }
                if (msg.length() > 0) {
                    if (capture) {
                        GlobalBuffer.append(msg);
                        if (GlobalCapture <= 0) {
                            // capture is now over, add additional line-feed
                            msg += "\n";;
                        }
                    }
                    Print.sysPrint(msg);
                }
            }
        };
        cst.setReadTimeout(200);
        cst.startThread();

        /* read/write stdin */
        for (;;) {
            try {
                String cmd = StringTools.trim(FileTools.readLineNL(System.in));
                if (cmd.equalsIgnoreCase("exit")) { 
                    break; 
                } else
                if (StringTools.startsWithIgnoreCase(cmd,"get")) { 
                    int p = cmd.indexOf(" ");
                    String sub = (p > 0)? cmd.substring(p+1).trim() : null;
                    if (StringTools.isBlank(sub)) {
                        cst.socketWriteLine("get");
                        continue;
                    } else
                    if (sub.equalsIgnoreCase("poll") || sub.equalsIgnoreCase("p")) {
                        cst.socketWriteLine("prompt off");
                        while (true) {
                            cst.socketWriteLine("get");
                            if (System.in.available() > 0) { break; } 
                            try { Thread.sleep(750L); } catch (Throwable th) {/*ignore*/}
                            if (System.in.available() > 0) { break; } 
                        }
                        cst.socketWriteLine("prompt on");
                        continue;
                    } else
                    if (sub.equalsIgnoreCase("cache") || sub.equalsIgnoreCase("c")) {
                        Print.sysPrintln(GlobalBuffer.toString());
                        cst.socketWriteLine(""); // send blank command to display prompt again
                        continue;
                    } else {
                        Print.sysPrintln("Unrecognized option: "+sub+"\n");
                        cst.socketWriteLine(""); // send blank command to display prompt again
                        continue;
                    }
                } else
                if (cmd.equalsIgnoreCase("clear")) { 
                    GlobalBuffer.setLength(0);
                    cst.socketWriteLine("clear"); // send "clear"
                    continue;
                } else
                if (StringTools.startsWithIgnoreCase(cmd,"save")) {
                    if (GlobalBuffer.length() == 0) {
                        Print.sysPrintln("Client GlobalBuffer is empty\n");
                        cst.socketWriteLine(""); // send blank command to display prompt again
                        continue;
                    }
                    int p = cmd.indexOf(" ");
                    String fileName = (p > 0)? cmd.substring(p+1).trim() : null;
                    if (StringTools.isBlank(fileName)) {
                        Print.sysPrintln("Save file not specified\n");
                        cst.socketWriteLine(""); // send blank command to display prompt again
                        continue;
                    }
                    File outFile = new File(fileName);
                    if (outFile.exists()) {
                        Print.sysPrintln("File already exists: "+outFile+"\n");
                        cst.socketWriteLine(""); // send blank command to display prompt again
                        continue;
                    }
                    try {
                        byte b[] = GlobalBuffer.toString().getBytes();
                        boolean ok = FileTools.writeFile(b, outFile);
                        if (ok) {
                            Print.sysPrintln("Output saved to file: "+outFile+"\n");
                        } else {
                            Print.sysPrintln("Unable to save output to file: "+outFile+"\n");
                        }
                        cst.socketWriteLine(""); // send blank command to display prompt again
                        continue;
                    } catch (IOException ioe) {
                        Print.sysPrintln("Error writing file: "+ioe+"\n");
                        cst.socketWriteLine(""); // send blank command to display prompt again
                        continue;
                    }
                } else
                if (cmd.equalsIgnoreCase("help")) { 
                    Print.sysPrintln(
                        "Help:\n" +
                        "  get        Gets/Resets the latest log contents from RemoteLogServer.\n" +
                        "  get poll   Continues \"get\" until Enter is pressed.\n" +
                        "  get cache  Displays the locally cached log contents.\n" +
                        "  clear      Clears/Resets the log contents (include local cache).\n" +
                        "  save FILE  Saves locally cached log contents to the specified file.\n" +
                        "  exit       Exit this RemoteLogClient\n" +
                        "  help       This help\n" +
                        "\n"
                        );
                    cst.socketWriteLine(""); // send blank command to display prompt again
                    continue;
                } else {
                    cst.socketWriteLine(cmd);
                    continue;
                }
            } catch (Throwable th) {
                Print.sysPrintln("ERROR: " + th);
                System.exit(1);
            }
        } // for(;;) 
        Print.sysPrintln("Terminating ...");
        System.exit(0);

    }

}
