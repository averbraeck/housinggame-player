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
    
    <div class="hg-title">Look for a house to buy</div>
    
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
        <div id="collapse1" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading1">
          <div class="panel-body">
            ${playerData.getContentHtml("panel/budget") }
          </div>
        </div>
      </div>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading2">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse2" aria-expanded="false" 
              aria-controls="collapse2" data-expandable="false">
              2. News for this round
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse2" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading2">
          <div class="panel-body">
            ${playerData.getContentHtml("news/summary/1") }
          </div>
        </div>
      </div>
      
      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading3">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse3" aria-expanded="false" 
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
            <p>
              Select a house from the list to check the key features. 
              You will pay ${playerData.getMortgagePercentage() }% 
              of the house price as the round mortgage. 
            </p>
            <p style="background-color:lightgrey;">
              You cannot select a house whose price is higher than the maximum mortgage
               (${playerData.k(playerData.getPlayerRound().getMaximumMortgage()) }) 
              + your savings (${playerData.k(playerData.getSavings()) })
              - your debt (${playerData.k(playerData.getDebt()) })
              = ${playerData.k(playerData.getMaxMortgagePlusSavings()) }
            </p>
            
            <form action="/housinggame-player/advance-state" method="post">
              <div class="form-group pmd-textfield form-group-sm">
                <label for="houses" class="control-label pmd-textfield-floating-label">Select house*</label> 
                <select name="houses" id="houses" class="form-control">
                   ${playerData.getContentHtml("house/options") }
                </select>
                ${playerData.getContentHtml("house/prices") }
              </div>
            </form>
            
            <div>
              ${playerData.getContentHtml("house/details") }
            </div>
          </div>
        </div>
      </div>

    </div>
    
   <form action="/housinggame-player/advance-state" method="post">
      <div class="hg-button">
        <input type="hidden" name="okButton" value="buy-house" />
        <input type="hidden" id="form-house-code" name="house" value="" />
        <input type="hidden" id="form-house-price" name="price" value="" />
        <input type="submit" value="BUY HOUSE" class="btn btn-primary" id="hg-submit" disabled />
      </div>
    </form>
    
    <br/>&nbsp;<br/>
    
  </div>
  
  <script>
    $(document).ready(function() {
      $(".house-details").hide();
      $(".house-price-label").hide();
      $(".house-price-input").hide();
      $("#house-details-" + $("#houses").find(":selected").text()).show();
      $("#house-price-label-" + $("#houses").find(":selected").text()).show();
      $("#house-price-input-" + $("#houses").find(":selected").text()).show();
      check();
    });
    $('#houses').on('change', function() {
    	// hide all house details
      $(".house-details").hide();
      $(".house-price-label").hide();
      $(".house-price-input").hide();
    	
    	// show the house details of the chosen house  
      $("#house-details-" + this.value).show();
      $("#house-price-label-" + this.value).show();
      $("#house-price-input-" + this.value).show();
      
      // fill return values
      $("#form-house-code").val(this.value);
      $("#form-house-price").val($("#house-price-input-" + this.value).val());
    });
    $('.house-price-input').on('input', function() {
      $("#form-house-price").val($("#house-price-input-" + $("#form-house-code").val()).val());
    });
    function check() {
        $.post("/housinggame-player/get-round-status", {jsp: 'buy-house'},
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
