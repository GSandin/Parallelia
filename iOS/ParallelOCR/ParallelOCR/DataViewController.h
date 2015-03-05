//
//  DataViewController.h
//  ParallelOCR
//
//  Created by Gustavo Sandín Carral on 5/3/15.
//  Copyright (c) 2015 Gustavo Sandín Carral. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface DataViewController : UIViewController

@property (strong, nonatomic) IBOutlet UILabel *dataLabel;
@property (strong, nonatomic) id dataObject;

@end

