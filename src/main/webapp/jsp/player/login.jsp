<%@page import="nl.tudelft.simulation.housinggame.player.SessionUtils"%>
<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
  
  <!-- Bootstrap css -->
  <link href="../../css/bootstrap.min.css" rel="stylesheet">
  
  <!-- Housinggame player css -->
  <link href="../../css/player.css" rel="stylesheet">
  
  <!-- MUI symbols -->
  <link href="../../iconfont/filled.css" rel="stylesheet">
  
  <!-- Propeller css -->
  <link href="../../css/propeller.min.css" rel="stylesheet">
  
  <!-- jQuery before Propeller.js -->
  <script type="text/javascript" src="../../js/jquery.min.js"></script>
  
  <!-- Include all compiled plugins (below), or include individual files as needed -->
  <script type="text/javascript" src="../../js/bootstrap.min.js"></script>
  <script type="text/javascript" src="../../js/propeller.min.js"></script>

  <title>Housing Game Player Login</title>

<style>
.form-container {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  min-height: 100vh;
  max-width: 30em;
  min-width: 20em;
  margin:auto;
}

form {
  max-width: 30em;
  
}

h1 {
  padding-bottom: 10px;
}

.hg-button {
  text-align: center;
  margin: auto;
}
</style>

</head>

<body>

<%
  if(session.getAttribute("playerData") == null) {
    PlayerData playerData = new PlayerData();
    session.setAttribute("playerData", playerData);
  }
%>

  <div class="form-container">
    
    <img src="images/hg-logo.png" width="50%" />
  
    <h1>Welcome to the Housing Game!</h1>
    
    <p style="text-align: center;">
      The aim is to achieve the highest score possible 
      by increasing satisfaction or earning points 
      for the available income.
    </p>
  
    <form action="/housinggame-player/login" method="post">
  
      <div class="form-group pmd-textfield form-group-sm">
        <label for="gamesession" class="control-label pmd-textfield-floating-label">Game session *</label> 
        <select name="gamesession" id="gamesession" class="form-control">
           ${playerData.getValidSessionOptions()}
        </select>
      </div>
        
      <div class="form-group pmd-textfield pmd-textfield-floating-label form-group-sm">
        <label for="group" class="control-label">Group *</label> 
        <input type="text" id="group" name="group" class="form-control" /> 
      </div>
        
      <div class="form-group pmd-textfield pmd-textfield-floating-label form-group-sm">
        <label for="password" class="control-label">Password *</label> 
        <input type="password" id="password" name="password" class="form-control" />
      </div>

      <div class="form-group pmd-textfield pmd-textfield-floating-label form-group-sm">
        <label for="username" class="control-label">Player code *</label> 
        <input type="text" id="username" name="username" class="form-control" />
      </div>

      <br /> 
      
      <div class="hg-button">
        <input type="submit" value="PLAYER LOGIN" class="btn btn-primary" />
      </div>
      
    </form>

    <br />

    <div style="padding-top: 10px;">
      <p style="font-size: 80%; text-align: center;">
        Interested to know more about the game?<br />
        Email to (<a href="mailto:v.j.cortesarevalo@tudelft.nl">v.j.cortesarevalo@tudelft.nl</a>).
      </p>
    </div>
  </div>
</body>
</html>
