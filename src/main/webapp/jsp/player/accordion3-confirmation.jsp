      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading3">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#hg-accordion" href="#collapse3" aria-expanded="false" 
              aria-controls="collapse3" data-expandable="false">
              3. Your house confirmation
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">keyboard_arrow_down</i>
            </a>
          </h4>
        </div>
        <div id="collapse3" class="panel-collapse collapse ${param.open}" role="tabpanel" aria-labelledby="heading3">
          <div class="panel-body">
            <p>
              ${playerData.getContentHtml("house/confirmation") }
            </p>
          </div>
        </div>
      </div>
