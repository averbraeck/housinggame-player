      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading1">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#hg-accordion" href="#collapse1" aria-expanded="true" 
              aria-controls="collapse1" data-expandable="false">
              1. Your budget and expectations
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">keyboard_arrow_down</i>
            </a>
          </h4>
        </div>
        <div id="collapse1" class="panel-collapse collapse ${param.open}" role="tabpanel" aria-labelledby="heading1">
          <div class="panel-body">
            ${playerData.getContentHtml("panel/budget") }
          </div>
        </div>
      </div>