<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game Welcome</title>
</head>

<body style="background-color: white;" onLoad = initPage()>

  <div class="form-container">
    
    <jsp:include page="header.jsp"></jsp:include>

    <h1 style="text-align: center; color: blue;">${playerData.getLabel("welcome/header") }</h1>
    
    <!--  accordion -->
    
    <div class="panel-group pmd-accordion" id="welcome-accordion" role="tablist" aria-multiselectable="true" > 
      
      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading1">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse1" aria-expanded="true" 
              aria-controls="collapse1" data-expandable="false">
              <span class="badge badge-info pmd-ripple-effect">1</span>
              ${playerData.getLabel("welcome/1/header") }
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse1" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="heading1">
          <div class="panel-body">
            ${playerData.getLabel("welcome/1/text") }
          </div>
        </div>
      </div>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading2">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse2" aria-expanded="false" 
              aria-controls="collapse2" data-expandable="false">
              ${playerData.getLabel("welcome/2/header") }
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse2" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading2">
          <div class="panel-body">
            ${playerData.getLabel("welcome/2/text") }
          </div>
        </div>
      </div>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading3">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse3" aria-expanded="false" 
              aria-controls="collapse3" data-expandable="false">
              <span class="badge badge-info pmd-ripple-effect">3</span>
              ${playerData.getLabel("welcome/3/header") }
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse3" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading3">
          <div class="panel-body">
            ${playerData.getLabel("welcome/3/text") }
          </div>
        </div>
      </div>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading4">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse4" aria-expanded="false" 
              aria-controls="collapse4" data-expandable="false">
              <span class="badge badge-info pmd-ripple-effect">4</span>
              ${playerData.getLabel("welcome/4/header") }
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse4" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading4">
          <div class="panel-body">
            ${playerData.getLabel("welcome/4/text") }
          </div>
        </div>
      </div>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading5">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse5" aria-expanded="false" 
              aria-controls="collapse5" data-expandable="false">
              <span class="badge badge-info pmd-ripple-effect">5</span>
              ${playerData.getLabel("welcome/5/header") }
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse5" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading5">
          <div class="panel-body">
            ${playerData.getLabel("welcome/5/text") }
          </div>
        </div>
      </div>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading6">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse6" aria-expanded="false" 
              aria-controls="collapse6" data-expandable="false">
              <span class="badge badge-info pmd-ripple-effect">6</span>
              ${playerData.getLabel("welcome/6/header") }
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse6" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading6">
          <div class="panel-body">
            ${playerData.getLabel("welcome/6/text") }
          </div>
        </div>
      </div>
      
    </div>
    
    <form action="/housinggame-player/news" method="post">
      <div class="hg-button">
        <input type="submit" value='${playerData.getLabel("welcome/button/finish") }' class="btn btn-primary" />
      </div>
    </form>
    
  </div>
</body>
</html>
