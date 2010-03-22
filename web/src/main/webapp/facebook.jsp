<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>

    <head>
        <title>Shortyz for Facebook</title>
        <script src="shortyz/shortyz.nocache.js" type="text/javascript"></script>
        <!-- <%=session.getAttribute("user.id")%> -->
    </head>

    <body>

        <iframe id="__gwt_historyFrame" style="width:0;height:0;border:0"></iframe>
        <!-- Note: Include this div markup as a workaround for a known bug in this release on IE where you may get a "operation aborted" error -->
        <div id="FB_HiddenIFrameContainer" style="display:none; position:absolute; left:-100px; top:-100px; width:0px; height: 0px;"></div>

        <script src="http://static.ak.connect.facebook.com/js/api_lib/v0.4/FeatureLoader.js.php" type="text/javascript"></script>
        <script type="text/javascript">
          FB_RequireFeatures(["CanvasUtil"], function(){
            FB.XdComm.Server.init("static/xd_receiver.html");
            FB.CanvasClient.startTimerToSizeToContent();
          });
        </script>
    </body>
</html>