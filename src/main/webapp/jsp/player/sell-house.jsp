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
    
    <div class="hg-title">Check available houses</div>
    
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
            <div class="hg-header1">Your options for a new house</div>
            <p>
              You can sell your house or stay in your current house. 
              Moving will cost you a one-time moving penalty.
              To be able to move, you need to find an affordable
              house. You will pay ${playerData.getMortgagePercentage() }% 
              of the house price as the round mortgage.
            </p>
            <p class="hg-box-grey">
              ${playerData.getContentHtml("house/affordable") }
            </p>
            <p>
              Note that your savings depend on the exact sales price of your house 
              (the above estimate is based on the sales value according to the bank).
              Also leave enough savings for paying your mortgage, taxes and potential damage after flooding.
            </p>
            
            <form>
              <div class="form-group pmd-textfield form-group-sm">
                <label for="houses" class="control-label pmd-textfield-floating-label">Select house*</label> 
                <select name="houses" id="houses" class="form-control">
                   ${playerData.getContentHtml("house/options") }
                </select>
                <br/>
                <div id="house-price-input-div">
                  <!--  will be filled with dynamic data -->
                </div>
              </div>
            </form>
            
            <div id="house-buy-feedback-div">
              <!--  will be filled with dynamic data -->
            </div>
            
            <div class="hg-header1">Selling price</div>
            <p>
              Based on the house's affordability options, do you still want to move?
              Then, you should sell your current house by agreeing on the selling price
              with the facilitator (if you sell to the bank) or with another player.
            </p>
            
            <form>
              <div class="form-group pmd-textfield form-group-sm">
                <label for="selling-price" class="control-label pmd-textfield-floating-label">Selling price (in k) *</label> 
                <input type="number" name="selling-price" id="selling-price" class="form-control" 
                  value='${playerData.getContentHtml("house/house-value") }'>
  
                <p>Once you set the price, wait for the facilitator to check and approve it!</p>
  
                <label for="selling-reason" class="control-label pmd-textfield-floating-label">Reason for selling *</label> 
                <select name="selling-reason" id="selling-reason" class="form-control">
                   ${playerData.getContentHtml("house/selling-reasons") }
                </select>
                <label for="selling-reason-other" class="control-label pmd-textfield-floating-label">Reason (in case of "Other")</label> 
                <input type="text" name="selling-reason-other" id="selling-reason-other" class="form-control">
              </div>
            </form>
          </div>
        </div>
      </div>

    </div>
    
    <div style="display:flex; flex-direction: row; justify-content: space-around;"> 
      <form action="/housinggame-player/sell-house-done" method="post">
        <div class="hg-button">
          <input type="hidden" name="nextScreen" value="stay-house-wait" />
          <input type="submit" value="STAY" class="btn btn-primary" id="hg-submit-stay" disabled />
        </div>
      </form>
  
      <form action="/housinggame-player/sell-house-done" method="post">
        <div class="hg-button">
          <input type="hidden" id="form-house-price" name="sell-price" value="" />
          <input type="hidden" id="form-house-reason" name="sell-reason" value="" />
          <input type="hidden" id="form-house-other" name="reason-other" value="" />
          <input type="hidden" name="nextScreen" value="sell-house-wait" />
          <input type="submit" value="SELL HOUSE" class="btn btn-primary" id="hg-submit-sell" disabled />
        </div>
      </form>
    </div>
        
    <br/>&nbsp;<br/>
    
  </div>
  
  <script>
  
    var choiceOk = false;
    var priceOk = true;
    var buttonOk = false;

    $(document).ready(function() {
      let price = $('#selling-price').val();
      $('#form-house-price').val(price);
      let other = $('#selling-reason-other').val();
      $('#form-house-other').val(other);
      $('#form-house-reason').val("NONE");
      check();
    });

    $('#houses').on('change', function() {
      let hgId = this.value;
      $.post("/housinggame-player/check-buy-house", {
        houseGroupId: this.value
      }, function(result, status) {
        json = JSON.parse(result);
        if (json.error.length > 0) {
          window.location.replace("/housinggame-player/error");
        } else {
          $('#house-price-input-div').replaceWith(json.housePriceInput);
          $('#house-buy-feedback-div').replaceWith(json.houseBuyFeedback);
        }
      });
    });
    
    function check() {
      $.post("/housinggame-player/get-round-status", {
        jsp: 'check-houses'
      }, function(data, status) {
        if (data == "OK") {
          buttonOk = true;
          $("#hg-submit-stay").removeAttr("disabled");
          if (choiceOk && priceOk) {
            $("#hg-submit-sell").removeAttr("disabled");
          }
        } else {
          buttonOk = false;
          setTimeout(check, 5000);
        }
      });
    }
    
    $('#selling-price').on('change', function() {
      let price = $('#selling-price').val();
      $('#form-house-price').val(price);
      priceOk = price.length > 0;
      if (buttonOk && choiceOk && priceOk) {
        $("#hg-submit-sell").removeAttr("disabled");
      } else {
        $("#hg-submit-sell").prop("disabled", true);
      }
    });
    
    $('#selling-reason').on('change', function() {
      let reason = this.value;
      $('#form-house-reason').val(reason);
      checkReason();
    });
    
    $('#selling-reason-other').on('change', function() {
      let other = $('#selling-reason-other').val();
      $('#form-house-other').val(other);
      checkReason();
    });
    
    function checkReason() {
      let c = $('select[name="selling-reason"]').find(':selected').attr('class');
      let other = $('#selling-reason-other').val();
      if (c == "reason-none") {
        choiceOk = false;
      } else if (c == "reason-value") {
        choiceOk = true;
      } else if (c == "reason-other" && other.length > 0) {
        choiceOk = true;
      } else {
        choiceOk = false;
      }
      if (buttonOk && choiceOk && priceOk) {
        $("#hg-submit-sell").removeAttr("disabled");
      } else {
        $("#hg-submit-sell").prop("disabled", true);
      }
    }

  </script>
  
</body>
</html>
