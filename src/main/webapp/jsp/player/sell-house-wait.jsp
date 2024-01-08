<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game: Select House</title>
  
<style>
.hg-house-row {
  display: flex;
  flex-direction: row;
  padding: 2px;
  margin: 2px;  
}

.hg-house-icon {
  padding: 2px;
  margin: 2px;
  display-vertical: center;
  opacity: 50%;
}
</style>

</head>

<body style="background-color: white;" onLoad = initPage()>

  <div class="form-container">
    
    <jsp:include page="header.jsp"></jsp:include>
    
    <div class="hg-title">Wait for the facilitator<br/>to check your selling request</div>
    
    <div class="panel-group pmd-accordion" id="hg-accordion" role="tablist" aria-multiselectable="true" > 
      <jsp:include page="accordion1.jsp"></jsp:include>
      <jsp:include page="accordion2.jsp"></jsp:include>
      <jsp:include page="accordion3.jsp">
        <jsp:param name="open" value="in" />
      </jsp:include>
    </div>
   
    <div style="display:flex; flex-direction: row; justify-content: space-around;"> 
      <form action="/housinggame-player/sell-house-accept-done" method="post">
        <div class="hg-button">
          <input type="hidden" name="nextScreen" value="accept-sell" />
          <input type="submit" value="APPROVED: MOVE OUT" class="btn btn-primary" id="hg-sell" disabled />
        </div>
      </form>
      <form action="/housinggame-player/sell-house-reject-done" method="post">
        <div class="hg-button">
          <input type="hidden" name="nextScreen" value="reject-sell" />
          <input type="submit" value="REJECTED: GO BACK" class="btn btn-primary" id="hg-goback" disabled />
        </div>
      </form>
    </div>
    
    <br/>&nbsp;<br/>
    
  </div>
  
  <script>
    $(document).ready(function() {
      check();
    });
    function check() {
        $.post("/housinggame-player/check-buy-status", {jsp: 'sell-house-wait'},
          function(data, status) {
            if (data == "APPROVED") {
              $("#hg-sell").removeAttr("disabled");
            } else if (data == "REJECTED") {
              $("#hg-goback").removeAttr("disabled");
            } else {
              setTimeout(check, 5000);
            }
          });
    }
  </script>
  
</body>
</html>
