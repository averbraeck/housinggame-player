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
    
    <div class="hg-title">Wait for the facilitator<br/>to check your buying request</div>
    
    <div class="panel-group pmd-accordion" id="hg-accordion" role="tablist" aria-multiselectable="true" > 
      <jsp:include page="accordion1.jsp"></jsp:include>
      <jsp:include page="accordion2.jsp"></jsp:include>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading3">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#hg-accordion" href="#collapse3" aria-expanded="false" 
              aria-controls="collapse3" data-expandable="false">
              3. Your house choice
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse3" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="heading3">
          <div class="panel-body">
            ${playerData.getContentHtml("house/wait-confirmation") }
          </div>
        </div>
      </div>

    </div>
   
   <div style="display:flex; flex-direction: row; justify-content: space-around;"> 
    <form action="/housinggame-player/advance-state" method="post">
      <div class="hg-button">
        <input type="hidden" name="okButton" value="move-in" />
        <input type="submit" value="APPROVED: MOVE IN" class="btn btn-primary" id="hg-movein" disabled />
      </div>
     </form>
     <form action="/housinggame-player/advance-state" method="post">
      <div class="hg-button">
        <input type="hidden" name="okButton" value="reject-buy" />
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
        $.post("/housinggame-player/check-buy-status", {jsp: 'buy-house-wait'},
          function(data, status) {
            if (data == "APPROVED") {
              $("#hg-movein").removeAttr("disabled");
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
