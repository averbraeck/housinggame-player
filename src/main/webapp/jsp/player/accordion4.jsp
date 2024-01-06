      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading4">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#hg-accordion" href="#collapse4" aria-expanded="false" 
              aria-controls="collapse4" data-expandable="false">
              4. Your community taxes
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">keyboard_arrow_down</i>
            </a>
          </h4>
        </div>
        <div id="collapse4" class="panel-collapse collapse ${param.open}" role="tabpanel" aria-labelledby="heading4">
          <div class="panel-body">
            <p>
              ${playerData.getContentHtml("panel/tax") }
            </p>
          </div>
        </div>
      </div>
