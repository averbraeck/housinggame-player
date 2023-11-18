	  <!--  header -->
	  <div class="hg-header">
	    <div class="hg-header-row">
	      <div class="hg-header-item" style="background-color: #abebc6;">
	        <span class="material-icons">group</span> 
	        ${playerData.getGroup().getName() }
	      </div>
        <div class="hg-header-item" style="background-color: #f9e79f;">
          <i class="material-icons">person</i>
          ${playerData.getPlayerCode() }
        </div>
        <div class="hg-header-item" style="background-color: #f9e79f;">
          <i class="material-icons">loop</i>
          ${playerData.getCurrentRound() }
        </div>
        <div class="hg-header-item" style="background-color: #bb8fce;">
          <i class="material-icons">credit_score</i>
          ${playerData.k(playerData.getPlayerRound().getLivingCosts()) }
        </div>
	    </div>

      <div class="hg-header-row">
        <div class="hg-header-item" style="background-color: #afd2ff;">
          <i class="material-icons">payments</i>
          ${playerData.k(playerData.getPlayerRound().getIncome()) }
        </div>
        <div class="hg-header-item" style="background-color: #f9e79f;">
          <i class="material-icons">stars</i>
          ${playerData.getPlayerRound().getSatisfaction() }
        </div>
        <div class="hg-header-item" style="background-color: #afd2ff;">
          <span class="material-icons">euro</span> 
          ${playerData.k(playerData.getPlayerRound().getSavings()) }
        </div>
        <div class="hg-header-item" style="background-color: #ec7063;">
          <i class="material-icons">credit_card</i>
          ${playerData.k(playerData.getPlayerRound().getDebt()) }
        </div>
        <div class="hg-header-item" style="background-color: #b5c1e9;">
          <i class="material-icons">account_balance</i>
          ${playerData.k(playerData.getPlayerRound().getCurrentWealth()) }
        </div>
      </div>

      <div class="hg-header-row">
        <div class="hg-header-item" style="background-color: #f8c471;">
          <i class="material-icons">water_damage</i>
          ${playerData.getPlayerRound().getPreferredHouseRating() }
        </div>
        <div class="hg-header-item" style="background-color: #e5e7e9;">
          <span class="material-icons">home</span> 
          <!-- {playerData.getPlayerRound().getHouseCode() } --> D01
        </div>
        <div class="hg-header-item" style="background-color: #e5e7e9;">
          <i class="material-icons">house</i>
          ${playerData.k(playerData.getPlayerRound().getMortgage()) }
        </div>
        <div class="hg-header-item" style="background-color: #bb8fce;">
          <i class="material-icons">price_check</i>
          ${playerData.k(playerData.getPlayerRound().getMaximumMortgage()) }
        </div>
      </div>

	  </div>
