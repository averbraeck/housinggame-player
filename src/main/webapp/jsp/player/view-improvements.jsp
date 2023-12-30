<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game Improvements</title>
  
  <style type="text/css">
form .checkbox label span {
  padding-left: 1.625em;
}

form .checkbox input[type="checkbox"] {
  left: 20px !important;
} 

  </style>
</head>

<body style="background-color: white;" onLoad = initPage()>

  <div class="form-container">
    
    <jsp:include page="header.jsp"></jsp:include>

    <h1 style="text-align: center; color: blue;">Select and buy improvements</h1>
    
    <div class="panel-group pmd-accordion" id="hg-accordion" role="tablist" aria-multiselectable="true" > 
      <jsp:include page="accordion1.jsp"></jsp:include>
      <jsp:include page="accordion2.jsp"></jsp:include>
      <jsp:include page="accordion3.jsp"></jsp:include>
      <jsp:include page="accordion4.jsp"></jsp:include>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading5">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#hg-accordion" href="#collapse5" aria-expanded="false" 
              aria-controls="collapse4" data-expandable="false">
              5. Buying improvements
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse5" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="heading5">
          <div class="panel-body">
            <p>
              ${playerData.getContentHtml("house/improvements") }
            </p>
            <div id="calculation-result">
              <p>
                You did not select any measures (yet).
              </p>
            </div>
          </div>
        </div>
      </div>

    </div>

   <form action="/housinggame-player/view-improvements-done" method="post">
      <div class="hg-button">
        <input type="hidden" name="nextScreen" value="answer-survey" />
        <input type="hidden" id="form-options" name="form-options" value="" />
        <input type="hidden" id="form-selected-points" name="form-selected-points" value="" />
        <input type="submit" value='BUY IMPROVEMENTS, ANSWER SURVEY' class="btn btn-primary" id="hg-submit" disabled />
      </div>
    </form>
    
  </div>
  
  <script>
  
    var choicesOk = true;
    var buttonOk = false;

    $(document).ready(function() {
      check();
    });
    
    function check() {
      $.post("/housinggame-player/get-round-status", {jsp: 'view-improvements'},
        function(data, status) {
          if (data == "OK" && choicesOk) {
            buttonOk = true;
            $("#hg-submit").removeAttr("disabled");
          } else {
            buttonOk = false;
            setTimeout(check, 5000);
          }
        });
    }
    
    $('input[type="checkbox"]').on('change', function() {
        checkCosts();
    });
    
    $('select').on('change', function() {
        checkCosts();
    });
    
    function checkCosts() {
        $('#form-options').val($('#improvements-form').serialize());
        $('#form-selected-points').val($('#selected-points').val());
        $.post("/housinggame-player/check-improvements-costs", {
        	  'jsp': 'view-improvements',
        	  'form': $('#improvements-form').serialize(),
        	  'selected-points': $('#selected-points').val()
        	}, function(result, status) {
        		json = JSON.parse(result);
            if (json.ok == "OK") {
              choicesOk = true;
              if (buttonOk)
            	  $("#hg-submit").removeAttr("disabled");
            } else {
            	choicesOk = false;
              $("#hg-submit").prop("disabled", true);
            }
            $('#calculation-result').replaceWith(json.html);
          });
    }
  </script>
  
</body>
</html>
