<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="head.jsp"></jsp:include>
	<title>Housing Game News</title>
</head>

<body style="background-color: white;" onLoad = initPage()>

	<div class="form-container">
		
		<jsp:include page="header.jsp"></jsp:include>
		
		<h1>NEWS</h1>
		
		<b>
		${playerData.getContentHtml("news/summary/1") }
		</b>
		
		<p style="margin-top: 1em;">
		${playerData.getContentHtml("news/content/1") }
		</p>
		
    <form action="/housinggame-player/new-house" method="post">
      <div class="hg-button">
        <input type="submit" value='${playerData.getLabel("welcome/button/finish") }' class="btn btn-primary" />
      </div>
    </form>
		
	</div>
</body>
</html>
