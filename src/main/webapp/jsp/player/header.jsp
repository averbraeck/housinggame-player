	  <!--  header -->
	  <div class="hg-header">
	    <div class="hg-header-row">
	      <div class="hg-header-item" style="background-color: white;">
  	      <img src="images/hg-logo.png" style="width:50px; height:auto" />
  	    </div>
  	    <div class="hg-header-item" style="background-color: white;">
  	      <h1>Where We Move</h1><br />
          <div class="hg-header-row">
            <div class="hg-header-item" style="background-color: white;">
              <i class="material-icons">news</i> News
  	        </div>
            <div class="hg-header-item" style="background-color: white;">
              <i class="material-icons">loop</i> Round ${playerData.getCurrentRound() }
            </div>
  	      </div>  
  	    </div>
  	  </div>
  	  
  	  <hr />
  	    
      <div class="hg-header-row">
	      <div class="hg-header-item" style="background-color: #abebc6;">
	        <span class="material-icons">group</span> 
	        ${playerData.getGroup().getName() }
	      </div>
        <div class="hg-header-item" style="background-color: #f9e79f;">
          <i class="material-icons">person</i>
          Player ${playerData.getPlayerCode() }
        </div>
        <div class="hg-header-item" style="background-color: #e5e7e9;">
          <i class="material-icons">house</i>
          House ${playerData.k(playerData.getPlayerRound().getMortgage()) }
        </div>
      </div>

      <div class="hg-header-row">
        <div class="hg-header-item" style="background-color: #abebc6;">
          <i class="material-icons">stars</i>
          Satisfaction ${playerData.getPlayerRound().getSatisfaction() }
        </div>
        <div class="hg-header-item" style="background-color: #f9e79f;">
          <i class="material-icons">payments</i>
          Available income ${playerData.k(playerData.getPlayerRound().getIncome()) }
        </div>
      </div>

	  </div>
