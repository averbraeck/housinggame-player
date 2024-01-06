      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading2">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#hg-accordion" href="#collapse2" aria-expanded="false" 
              aria-controls="collapse2" data-expandable="false">
              2. News for this round
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">keyboard_arrow_down</i>
            </a>
          </h4>
        </div>
        <div id="collapse2" class="panel-collapse collapse ${param.open}" role="tabpanel" aria-labelledby="heading2">
          <div class="panel-body">
            ${playerData.getContentHtml("panel/news") }
          </div>
        </div>
      </div>
