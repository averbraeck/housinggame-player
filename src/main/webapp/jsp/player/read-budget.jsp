<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game Budget Overview</title>
</head>

<body style="background-color: white;" onLoad = initPage()>

	<div class="form-container">
		
    <jsp:include page="header.jsp"></jsp:include>

	  <h1 style="text-align: center; color: blue;">Budget overview</h1>
	  
	  <p style="text-align: center; padding: 10px;">
	    The aim is to achieve the highest score possible by increasing 
	    satisfaction and earning points for the available income.<br>
      While playing, you will learn which choices you may make for protecting 
      your house and yourself, given the assigned budget and expectations
	  </p>
	  
	  <div class="panel-group pmd-accordion" id="welcome-accordion" role="tablist" aria-multiselectable="true" > 
      
      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading1">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse1" aria-expanded="true" 
              aria-controls="collapse1" data-expandable="false">
              1. Your budget and expectations
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse1" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="heading1">
          <div class="panel-body">
            ${playerData.getContentHtml("panel/budget") }
          </div>
        </div>
      </div>
	  
	  </div>
	  
    <form action="/housinggame-player/advance-state" method="post">
      <div class="hg-button">
        <input type="hidden" name="okButton" value="read-budget" />
        <input type="submit" value='${playerData.getLabel("welcome/button/finish") }' class="btn btn-primary" id="hg-submit" disabled />
      </div>
    </form>
		
	</div>
	
  <script>
    $(document).ready(function() {
      check();
    });
    function check() {
      $.post("/housinggame-player/get-round-status", {jsp: 'read-budget'},
        function(data, status) {
          if (data == "OK") {
            $("#hg-submit").removeAttr("disabled");
          } else {
            setTimeout(check, 5000);
          }
        });
    }
  </script>
  
</body>
</html>
