	  <!--  header -->
	  <div class="hg-header">
	    <div class="hg-header-row">
	      <div class="hg-header-item" style="background-color: white;">
  	      <img src="images/hg-logo.png" style="width:90px; height:auto" />
  	    </div>
  	    <div class="hg-header-item" style="background-color: white;">
  	      <h1>Where We Move</h1>
          <div class="hg-header-row">
            <div class="hg-header-item" style="background-color: white;">
              <i class="material-icons">loop</i> Round ${playerData.getPlayerRoundNumber() }
            </div>
            <div class="hg-header-item" style="background-color: white;">
              <i class="material-icons">logout</i> <a href="/housinggame-player" style="color:black;">Logout</a>
            </div>
  	      </div>  
  	    </div>
  	  </div>
  	  
      <div style="border-top: 1px solid; width:100%; border-color:blue; margin-bottom: 8px;"></div>
  	  
      <div class="hg-header-row">
	      <div class="hg-header-item" style="background-color: #f9e79f;">
	        <span class="material-icons">group</span> 
	        ${playerData.getGroup().getName() }
	      </div>
        <div class="hg-header-item" style="background-color: #f9e79f;">
          <i class="material-icons">person</i>
          Player ${playerData.getPlayerCode() }
        </div>
        <div class="hg-header-item" style="background-color: #c9c9c9;">
          <i class="material-icons">house</i>
          House ${playerData.getHouseCode() }
        </div>
      </div>

      <div class="hg-header-row">
        <div class="hg-header-item" style="background-color: #91c7c9;">
          <i class="material-icons">stars</i>
          Satisfaction ${playerData.getTotalSatisfaction() }
        </div>
        <div class="hg-header-item" style="background-color: #91c7c9;">
          <i class="material-icons">payments</i>
          Spendable income ${playerData.k(playerData.getPlayerRound().getSpendableIncome()) }
        </div>
      </div>

      <div style="border-top: 1px solid; width:99%; border-color:blue; margin-top: 12px; margin-bottom: 3px;"></div>

	  </div>
