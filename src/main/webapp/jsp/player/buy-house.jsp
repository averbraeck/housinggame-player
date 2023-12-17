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

#collapse3 select {
  background: #CCE6FF !important;
}

#collapse3 input {
  background: #CCE6FF !important;
}

option:not(:checked) {
  background-color: #fff;
}
</style>

</head>

<body style="background-color: white;" onLoad = initPage()>

  <div class="form-container">
    
    <jsp:include page="header.jsp"></jsp:include>
    
    <div class="hg-title">Look for a house to buy</div>
    
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
            <p>
              Select a house from the list to check the key features. 
              You will have to pay ${playerData.getMortgagePercentage()}% 
              of the mortgage on the house to the bank in every round. 
            </p>
            <p class="hg-box-grey">
              You cannot select a house whose price is higher than the maximum mortgage
               (${playerData.k(playerData.getPlayerRound().getMaximumMortgage()) }) 
              + your savings (${playerData.k(playerData.getSavings()) })
              - your debt (${playerData.k(playerData.getDebt()) })
              = ${playerData.k(playerData.getMaxMortgagePlusSavings()) }
            </p>
            
            <form action="/housinggame-player/advance-state" method="post">
              <div class="form-group pmd-textfield form-group-sm">
                <label for="houses" class="control-label pmd-textfield-floating-label">Select house*</label> 
                <select name="houses" id="houses" class="form-control"">
                   ${playerData.getContentHtml("house/options") }
                </select>
                <br/>
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
