      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading7">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#hg-accordion" href="#collapse7" aria-expanded="false" 
              aria-controls="collapse7" data-expandable="false">
              7. View round summary
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">keyboard_arrow_down</i>
            </a>
          </h4>
        </div>
        <div id="collapse7" class="panel-collapse collapse ${param.open}" role="tabpanel" aria-labelledby="heading7">
          <div class="panel-body">
            <p>
              ${playerData.getContentHtml("summary/content") }
            </p>
          </div>
        </div>
      </div>
      