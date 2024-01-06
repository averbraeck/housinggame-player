<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game View Damage</title>

<style>
.hg-table tbody tr td:nth-of-type(2n), .hg-table thead tr th:nth-of-type(2n) {
  border-right: 1px solid #4285F4;
  border-collapse: collapse;
}

.hg-table tbody tr td:nth-of-type(1), .hg-table thead tr th:nth-of-type(1) {
  border-left: 1px solid #4285F4;
  border-collapse: collapse;
}

.hg-table {
  border-bottom: 1px solid #4285F4;
  border-collapse: collapse;
}
</style>

</head>

<body style="background-color: white;" onLoad = initPage()>

  <div class="form-container">
    
    <jsp:include page="header.jsp"></jsp:include>

    <h1 style="text-align: center; color: blue;">View the damage to your house</h1>
    
    <div class="panel-group pmd-accordion" id="hg-accordion" role="tablist" aria-multiselectable="true" > 
      <jsp:include page="accordion1.jsp"></jsp:include>
      <jsp:include page="accordion2.jsp"></jsp:include>
      <jsp:include page="accordion3.jsp"></jsp:include>
      <jsp:include page="accordion4.jsp"></jsp:include>
      <jsp:include page="accordion5.jsp"></jsp:include>
      <jsp:include page="accordion6.jsp">
        <jsp:param name="open" value="in" />
      </jsp:include>
    </div>

    <form action="/housinggame-player/view-damage-done" method="post">
      <div class="hg-button">
        <input type="hidden" name="nextScreen" value="view-summary" />
        <input type="submit" value='VIEW ROUND SUMMARY' class="btn btn-primary" id="hg-submit" disabled />
      </div>
    </form>
    
  </div>
  
  <script>
    $(document).ready(function() {
      check();
    });
    function check() {
      $.post("/housinggame-player/get-round-status", {jsp: 'view-damage'},
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
